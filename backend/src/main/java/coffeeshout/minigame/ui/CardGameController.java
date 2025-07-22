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
        cardGameService.start(roomId);

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                MiniGameStateMessage.from(cardGame));
    }

    @MessageMapping("/room/{roomId}/cardGame/select")
    public void selectCard(@DestinationVariable Long roomId, @Payload CardGameSelectMessage message) {
        cardGameService.selectCard(roomId, message.playerName(), message.cardIndex());

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        // 선택만 하면 될듯
        // 다음 라운드 진행은 호스트가 결정하는거 아닌가?
        // 아니면 클라이언트가 결정?
        // select가 끝나면 그냥 바로 시작되도 되는건가?

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                MiniGameStateMessage.from(cardGame));

        cardGameService.checkAndMoveRound(roomId);
    }

    @MessageMapping("/room/{roomid}/cardGame/rank")
    public void getRank(@DestinationVariable Long roomId) {
        final MiniGameResult miniGameResult = cardGameService.getMiniGameResult(roomId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/  ",
                coffeeshout.minigame.ui.MiniGameRanksMessage.from(miniGameResult));
    }
}
