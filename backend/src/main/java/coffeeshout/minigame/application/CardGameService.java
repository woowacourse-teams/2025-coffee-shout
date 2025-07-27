package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor.CardGameTask;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";

    private final RoomQueryService roomQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;

    public void startGame(JoinCode joinCode) {
        final Room room = roomQueryService.findByJoinCode(joinCode);

        room.startGame(MiniGameType.CARD_GAME);
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

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomJoinCode);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.get(roomJoinCode).cancelPlaying();
        }
    }

    private CardGameTask play(JoinCode joinCode) {
        CardGame cardGame = getCardGame(joinCode);
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
                () -> getCardGame(joinCode).changeScoreBoardState(),
                () -> sendCardGameState(joinCode),
                () -> {
                }
        );
    }

    private CardGameTask loading(JoinCode joinCode) {
        return new CardGameTask(
                CardGameState.LOADING,
                () -> getCardGame(joinCode).changeLoadingState(),
                () -> sendCardGameState(joinCode),
                () -> {
                }
        );
    }

    private CardGameTask done(JoinCode joinCode) {
        return new CardGameTask(
                CardGameState.DONE,
                () -> getCardGame(joinCode).changeDoneState(),
                () -> sendCardGameResult(joinCode),
                () -> {
                }
        );
    }

    private void sendCardGameState(JoinCode joinCode) {
        CardGame cardGame = getCardGame(joinCode);
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(JoinCode joinCode) {
        CardGame cardGame = getCardGame(joinCode);
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, cardGame.getResult());
    }

    private CardGame getCardGame(JoinCode joinCode) {
        Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
