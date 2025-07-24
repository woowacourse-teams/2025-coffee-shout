package coffeeshout.minigame.ui;

import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
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
    private final CardGameQueryService cardGameQueryService;

    @MessageMapping("/room/{joinCode}/cardGame/start")
    public void startGame(@DestinationVariable String joinCode) {
        cardGameService.startGame(new JoinCode(joinCode));
    }

    @MessageMapping("/room/{joinCode}/cardGame/select")
    public void selectCard(@DestinationVariable String joinCode, @Payload CardGameSelectMessage message) {
        JoinCode roomJoinCode = new JoinCode(joinCode);
        cardGameService.selectCard(roomJoinCode, message.playerName(), message.cardIndex());

        final CardGame cardGame = cardGameQueryService.getCardGame(roomJoinCode);
        messagingTemplate.convertAndSend(
                "/topic/room/" + joinCode + "/gameState",
                MiniGameStateMessage.from(cardGame)
        );
    }

    @MessageMapping("/room/{joinCode}/cardGame/rank")
    public void getRank(@DestinationVariable String joinCode) {
        JoinCode roomJoinCode = new JoinCode(joinCode);
        final MiniGameResult miniGameResult = cardGameQueryService.getCardGame(roomJoinCode).getResult();

        messagingTemplate.convertAndSend(
                "/topic/room/" + joinCode + "/rank",
                coffeeshout.minigame.ui.MiniGameRanksMessage.from(miniGameResult)
        );
    }
}
