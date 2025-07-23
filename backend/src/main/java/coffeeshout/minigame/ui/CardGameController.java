package coffeeshout.minigame.ui;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CardGameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameService cardGameService;

    @MessageMapping("/room/{roomId}/cardGame/start")
    public void startGame(@DestinationVariable Long roomId) {
        cardGameService.startGame(roomId);

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/gameState",
                MiniGameStateMessage.from(cardGame)
        );
    }

    @MessageMapping("/room/{roomId}/cardGame/select")
    public void selectCard(@DestinationVariable Long roomId, @Payload CardGameSelectMessage message) {
        cardGameService.selectCard(roomId, message.playerName(), message.cardIndex());

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/gameState",
                MiniGameStateMessage.from(cardGame)
        );
    }

    @MessageMapping("/room/{roomId}/cardGame/rank")
    public void getRank(@DestinationVariable Long roomId) {
        final MiniGameResult miniGameResult = cardGameService.getMiniGameResult(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/rank",
                coffeeshout.minigame.ui.MiniGameRanksMessage.from(miniGameResult)
        );
    }
}
