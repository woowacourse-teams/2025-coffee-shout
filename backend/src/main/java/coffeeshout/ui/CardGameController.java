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

    @MessageMapping("/cardGame/rank")
    public void getRank(CardGameRankMessage message) {
        final MiniGameResult miniGameResult = cardGameService.getMiniGameResult(message.roomId());

        messagingTemplate.convertAndSend("/topic/gameRanks/", MiniGameRanksMessage.from(miniGameResult));
    }
}
