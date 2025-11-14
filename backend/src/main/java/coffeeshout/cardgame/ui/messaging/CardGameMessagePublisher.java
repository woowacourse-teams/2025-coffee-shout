package coffeeshout.cardgame.ui.messaging;

import coffeeshout.cardgame.domain.CardGame;
import coffeeshout.cardgame.domain.event.dto.CardGameStateChangedEvent;
import coffeeshout.cardgame.domain.event.dto.MiniGameStartedEvent;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CardGame 도메인의 상태 변경을 WebSocket을 통해 브로드캐스트하는 Message Publisher
 * <p>
 * Spring Domain Event를 구독하여 WebSocket 메시지를 전송함으로써
 * 비즈니스 로직과 브로드캐스트 로직을 분리합니다.
 * </p>
 * <p>
 * 이 클래스는 Presentation Layer (UI)에 위치하며, 사용자에게 보여지는 WebSocket 응답을 관리합니다.
 * Response 객체 생성과 API 문서화를 함께 담당하여 일관성을 유지합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CardGameMessagePublisher {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final LoggingSimpMessagingTemplate messagingTemplate;

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    @Operation(
            summary = "카드게임 상태 브로드캐스트",
            description = "카드게임의 현재 상태를 방의 모든 참가자에게 전송합니다."
    )
    public void publishCardGameStateChanged(CardGameStateChangedEvent cardGameStateChangedEvent) {
        final Room room = cardGameStateChangedEvent.room();
        final CardGame cardGame = cardGameStateChangedEvent.cardGame();
        sendCardGameState(cardGame, room.getJoinCode());
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/round",
            returnType = MiniGameStartMessage.class
    )
    @Operation(
            summary = "카드게임 시작 브로드캐스트",
            description = "카드게임이 시작되었음을 방의 모든 참가자에게 전송합니다."
    )
    public void publishCardGameStarted(MiniGameStartedEvent miniGameStartedEvent) {
        final MiniGameType miniGameType = MiniGameType.valueOf(miniGameStartedEvent.gameType());
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, miniGameStartedEvent.joinCode()),
                WebSocketResponse.success(new MiniGameStartMessage(miniGameType))
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/gameState",
            returnType = MiniGameStateMessage.class
    )
    @Operation(
            summary = "카드 선택 브로드캐스트",
            description = "플레이어가 카드를 선택했을 때 변경된 게임 상태를 방의 모든 참가자에게 전송합니다."
    )
    public void publishCardSelected(CardSelectedEvent cardSelectedEvent) {
        sendCardGameState(cardSelectedEvent.cardGame(), cardSelectedEvent.joinCode());
    }

    private void sendCardGameState(CardGame cardGame, JoinCode joinCode) {
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }
}
