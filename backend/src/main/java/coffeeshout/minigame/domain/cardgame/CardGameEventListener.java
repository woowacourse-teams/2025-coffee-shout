package coffeeshout.minigame.domain.cardgame;

import coffeeshout.generator.MessageResponse;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardGameStateChangeEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardGameEventListener {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final LoggingSimpMessagingTemplate messagingTemplate;

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    public void handleSelectCard(CardSelectEvent cardSelectEvent) {
        sendCardGameState(cardSelectEvent.cardGame(), cardSelectEvent.joinCode());
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    public void handleChangeState(CardGameStateChangeEvent cardGameStateChangeEvent) {
        sendCardGameState(cardGameStateChangeEvent.cardGame(), cardGameStateChangeEvent.joinCode());
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/round",
            returnType = MiniGameStartMessage.class
    )
    public void handleStart(CardGameStartEvent cardGameStartEvent) {
        CardGame cardGame = cardGameStartEvent.cardGame();
        JoinCode joinCode = cardGameStartEvent.joinCode();
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode.value()),
                WebSocketResponse.success(new MiniGameStartMessage(cardGame.getMiniGameType()))
        );
    }

    private void sendCardGameState(CardGame cardSelectEvent, JoinCode cardSelectEvent1) {
        CardGame cardGame = cardSelectEvent;
        JoinCode joinCode = cardSelectEvent1;
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.value());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }
}
