package coffeeshout.minigame.domain.cardgame.event;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CardGameWebSocketBroker {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final LoggingSimpMessagingTemplate messagingTemplate;

    public CardGameWebSocketBroker(LoggingSimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    @Operation(
            summary = "카드 게임 상태 변경 이벤트 처리",
            description = """
                    카드 게임의 상태가 변경되었을 때 발생하는 이벤트를 처리합니다.
                    게임 진행 상태의 변화를 감지하고 해당 방의 모든 참가자에게 업데이트된 게임 상태를 전달합니다.
                    """
    )
    public void sendCardGameState(CardGameStateChangedEvent cardGameStateChangedEvent) {
        final CardGame cardGame = cardGameStateChangedEvent.cardGame();
        final Room room = cardGameStateChangedEvent.room();
        sendCardGameState(cardGame, room.getJoinCode());
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    @Operation(
            summary = "카드 선택 이벤트 처리 및 게임 상태 브로드캐스트",
            description = """
                    카드 게임에서 플레이어가 카드를 선택했을 때 발생하는 이벤트를 처리합니다.
                    게임 상태를 업데이트하고 해당 방의 모든 참가자에게 최신 게임 상태를 브로드캐스트합니다.
                    """
    )
    public void sendCardGameState(CardSelectedEvent cardSelectedEvent) {
        sendCardGameState(cardSelectedEvent.cardGame(), cardSelectedEvent.joinCode());
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/round",
            returnType = MiniGameStartMessage.class
    )
    @Operation(
            summary = "카드 게임 시작 이벤트 처리 및 라운드 시작 알림",
            description = """
                    카드 게임이 시작될 때 발생하는 이벤트를 처리합니다.
                    새로운 라운드가 시작되었음을 해당 방의 모든 참가자에게 알리고
                    게임 타입 정보를 포함한 시작 메시지를 브로드캐스트합니다.
                    """
    )
    public void sendMiniGameType(CardGameStartedEvent cardGameStartedEvent) {
        CardGame cardGame = cardGameStartedEvent.cardGame();
        JoinCode joinCode = cardGameStartedEvent.joinCode();
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode.getValue()),
                WebSocketResponse.success(new MiniGameStartMessage(cardGame.getMiniGameType()))
        );
    }

    private void sendCardGameState(CardGame cardGame, JoinCode joinCode) {
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }
}
