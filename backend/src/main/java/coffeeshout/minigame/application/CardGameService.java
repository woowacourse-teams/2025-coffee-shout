package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor.CardGameTask;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";

    private final CardGameQueryService cardGameQueryService;
    private final RoomFinder roomFinder;
    private final CardGameRepository cardGameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;

    public void startGame(JoinCode joinCode) {
        final Room room = roomFinder.findByJoinCode(joinCode);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(
                deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT),
                room.getPlayers()
        );
        cardGameRepository.save(joinCode, cardGame);
        CardGameTaskExecutor executor = new CardGameTaskExecutor(List.of(
                loading(joinCode), // 로딩
                play(joinCode), // 1라운드 시작
                scoreBoard(joinCode), // 1라운드 결과
                loading(joinCode), // 1라운드 끝나고 로딩
                play(joinCode), // 2라운드 시작
                scoreBoard(joinCode), // 2라운드 끝나고 결과
                done(joinCode)
        ));
        cardGameTaskExecutors.put(joinCode, executor);
        executor.submits();
    }

    public void selectCard(JoinCode joinCode, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(joinCode);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.get(joinCode).cancelPlaying();
        }
    }

    private CardGameTask play(JoinCode joinCode) {
        CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        return new CardGameTask(
                CardGameState.PLAYING,
                cardGame::startRound,
                () -> sendCardGameState(joinCode),
                cardGame::assignRandomCardsToUnselectedPlayers
        );
    }

    private CardGameTask scoreBoard(JoinCode joinCode) {
        return new CardGameTask(
                CardGameState.SCORE_BOARD,
                () -> cardGameQueryService.getCardGame(joinCode).changeScoreBoardState(),
                () -> sendCardGameState(joinCode),
                () -> {}
        );
    }

    private CardGameTask loading(JoinCode joinCode) {
        return new CardGameTask(
                CardGameState.LOADING,
                () -> cardGameQueryService.getCardGame(joinCode).changeLoadingState(),
                () -> sendCardGameState(joinCode),
                () -> {}
        );
    }

    private CardGameTask done(JoinCode joinCode) {
        return new CardGameTask(
                CardGameState.DONE,
                () -> cardGameQueryService.getCardGame(joinCode).changeDoneState(),
                () -> sendCardGameResult(joinCode),
                () -> {}
        );
    }

    private void sendCardGameState(JoinCode joinCode) {
        CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(JoinCode joinCode) {
        CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, cardGame.getResult());
    }
}
