package coffeeshout.ui;

import coffeeshout.application.CardGameService;
import coffeeshout.domain.CardGame;
import coffeeshout.ui.request.CardGameSelectMessage;
import coffeeshout.ui.request.CardGameStartMessage;
import coffeeshout.ui.response.CardDto;
import coffeeshout.ui.response.GameStateMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CardGameController {

    private final SimpMessagingTemplate messagingTemplate;

    private final CardGameService cardGameService;

    @MessageMapping("/cardGame/start")
    public void startGame(CardGameStartMessage message) {
        cardGameService.start(message.roomId());

        final CardGame cardGame = cardGameService.getCardGame(message.roomId());

        sendGameState(cardGame, message.roomId());
    }

    @MessageMapping("/cardGame/select")
    public void selectCard(CardGameSelectMessage message) {
        cardGameService.selectCard(message.roomId(), message.playerId(), message.cardPosition());

        final CardGame cardGame = cardGameService.getCardGame(message.roomId());

        sendGameState(cardGame, message.roomId());

        cardGameService.checkRound(message.roomId());
    }


    private void sendGameState(CardGame cardGame, Long roomId) {
        final Map<Long, Integer> scores = generatePlayerScores(cardGame);
        final Map<CardDto, Long> playerSelections = generatePlayerSelections(cardGame);

        GameStateMessage stateMessage = new GameStateMessage(
                roomId,
                cardGame.getRound().getValue(),
                playerSelections,
                scores,
                cardGame.isFirstRoundFinished()
        );

        messagingTemplate.convertAndSend("/topic/gameState/" + roomId, stateMessage);
    }

    private Map<CardDto, Long> generatePlayerSelections(CardGame cardGame) {
        Map<CardDto, Long> playerSelections = new HashMap<>();
        cardGame.getCards()
                .forEach(card -> playerSelections.put(
                        CardDto.from(card),
                        cardGame.findCardHolder(card, cardGame.getRound().getValue()).getId()
                ));

        return playerSelections;
    }

    private Map<Long, Integer> generatePlayerScores(CardGame cardGame) {
        Map<Long, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getId(), score));

        return scores;
    }
}
