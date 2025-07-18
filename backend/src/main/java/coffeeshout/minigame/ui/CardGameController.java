package coffeeshout.minigame.ui;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
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

        messagingTemplate.convertAndSend("/topic/room/" + message.roomId() + "/gameState",
                MiniGameStateMessage.of(cardGame, message.roomId()));
    }

    @MessageMapping("/cardGame/select")
    public void selectCard(CardGameSelectMessage message) {
        cardGameService.selectCard(message.roomId(), message.playerName(), message.cardIndex());

        final CardGame cardGame = cardGameService.getCardGame(message.roomId());

        messagingTemplate.convertAndSend("/topic/room/" + message.roomId() + "/gameState",
                MiniGameStateMessage.of(cardGame, message.roomId()));

        cardGameService.checkAndMoveRound(message.roomId());
    }

    @MessageMapping("/cardGame/rank")
    public void getRank(CardGameRankMessage message) {
        final MiniGameResult miniGameResult = cardGameService.getMiniGameResult(message.roomId());

        messagingTemplate.convertAndSend("/topic/room/" + message.roomId() + "/  ",
                coffeeshout.minigame.ui.MiniGameRanksMessage.from(miniGameResult));
    }
}
