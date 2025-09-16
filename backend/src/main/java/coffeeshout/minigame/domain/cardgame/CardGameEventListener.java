package coffeeshout.minigame.domain.cardgame;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.minigame.MiniGameRoundProgressEvent;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardGameStateChangeEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardGameEventListener {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;

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
    public void handleChangeState(CardGameStateChangeEvent cardGameStateChangeEvent) {
        CardGame cardGame = cardGameStateChangeEvent.cardGame();
        JoinCode joinCode = cardGameStateChangeEvent.joinCode();
        
        // 기존 웹소켓 전송 (LoggingSimpMessagingTemplate이 Redis 동기화도 처리함)
        sendCardGameState(cardGame, joinCode);
        
        // 🔥 추가: 카드게임 상태 변경 Redis 동기화
        try {
            CardGameSnapshot snapshot = cardGame.createSnapshot();
            messagePublisher.publishMiniGameRoundProgress(new MiniGameRoundProgressEvent(
                joinCode.getValue(),
                cardGame.getMiniGameType(),
                snapshot,
                instanceConfig.getInstanceId()
            ));
        } catch (Exception e) {
            // 동기화 실패해도 게임은 계속 진행
        }
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
    public void handleSelectCard(CardSelectEvent cardSelectEvent) {
        // 카드 선택은 이미 CardGameService에서 Redis 동기화됨
        // 여기서는 웹소켓 전송만 처리 (LoggingSimpMessagingTemplate이 Redis 동기화도 처리함)
        sendCardGameState(cardSelectEvent.cardGame(), cardSelectEvent.joinCode());
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
    public void handleStart(CardGameStartEvent cardGameStartEvent) {
        CardGame cardGame = cardGameStartEvent.cardGame();
        JoinCode joinCode = cardGameStartEvent.joinCode();
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode.getValue()),
                WebSocketResponse.success(new MiniGameStartMessage(cardGame.getMiniGameType()))
        );
    }

    private void sendCardGameState(CardGame cardSelectEvent, JoinCode cardSelectEvent1) {
        CardGame cardGame = cardSelectEvent;
        JoinCode joinCode = cardSelectEvent1;
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }
}
