package coffeeshout.room.application.messaging;

import coffeeshout.global.messaging.RedisStreamBroadcastService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomBroadcastStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    public static final String COMPLETION_STREAM = "room:broadcast:completions";

    // 응답 대기 중인 요청들을 관리
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();


    public void broadcastEnterRoom(
            String joinCode,
            String playerName,
            SelectedMenuRequest selectedMenuRequest
    ) {
        log.info("Broadcasting enter room event (async): joinCode={}, playerName={}", joinCode, playerName);

        String requestId = UUID.randomUUID().toString();

        try {
            Map<String, Object> requestData = Map.of(
                    "joinCode", joinCode,
                    "playerName", playerName,
                    "selectedMenuRequest", selectedMenuRequest
            );

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RedisStreamBroadcastService.BROADCAST_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "type", "ENTER_ROOM_BROADCAST",
                            "data", objectMapper.writeValueAsString(requestData),
                            "sender", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.info("Enter room broadcast sent (async): requestId={}, recordId={}", requestId, recordId.getValue());

        } catch (Exception e) {
            log.error("Failed to broadcast enter room event (async)", e);
        }
    }

    /**
     * 동기적으로 방 입장을 브로드캐스트하고 결과를 기다림
     */
    public CompletableFuture<Object> broadcastEnterRoomSync(
            String joinCode,
            String playerName,
            SelectedMenuRequest selectedMenuRequest
    ) {
        log.info("Broadcasting enter room event (sync): joinCode={}, playerName={}", joinCode, playerName);

        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Object> future = new CompletableFuture<>();

        // 응답 대기 등록
        pendingRequests.put(requestId, future);

        try {
            Map<String, Object> requestData = Map.of(
                    "joinCode", joinCode,
                    "playerName", playerName,
                    "selectedMenuRequest", selectedMenuRequest
            );

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RedisStreamBroadcastService.BROADCAST_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "type", "ENTER_ROOM_BROADCAST",
                            "data", objectMapper.writeValueAsString(requestData),
                            "sender", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.info("Enter room broadcast sent (sync): requestId={}, recordId={}", requestId, recordId.getValue());

            // 10초 후 타임아웃
            CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
                if (!future.isDone()) {
                    pendingRequests.remove(requestId);
                    future.completeExceptionally(new RuntimeException("Broadcast timeout: " + requestId));
                }
            });

            return future;

        } catch (Exception e) {
            pendingRequests.remove(requestId);
            log.error("Failed to broadcast enter room event (sync)", e);
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 방 상태 업데이트를 모든 인스턴스에 브로드캐스트
     */
    public void broadcastUpdateRoomState(String joinCode, Object roomState) {
        log.info("Broadcasting room state update: joinCode={}", joinCode);

        String requestId = UUID.randomUUID().toString();

        try {
            Map<String, Object> requestData = Map.of(
                    "joinCode", joinCode,
                    "roomState", roomState
            );

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RedisStreamBroadcastService.BROADCAST_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "type", "UPDATE_ROOM_STATE",
                            "data", objectMapper.writeValueAsString(requestData),
                            "sender", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.info("Room state update broadcast sent: requestId={}, recordId={}", requestId, recordId.getValue());
        } catch (Exception e) {
            log.error("Failed to broadcast room state update", e);
            throw new RuntimeException("Failed to broadcast room state update", e);
        }
    }

    /**
     * 방 데이터 동기화를 모든 인스턴스에 브로드캐스트
     */

    public String broadcastSyncRoomData(String joinCode, Object roomData) {
        log.info("Broadcasting room data sync: joinCode={}", joinCode);

        String requestId = UUID.randomUUID().toString();

        try {
            Map<String, Object> requestData = Map.of(
                    "joinCode", joinCode,
                    "roomData", roomData
            );

            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RedisStreamBroadcastService.BROADCAST_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "type", "SYNC_ROOM_DATA",
                            "data", objectMapper.writeValueAsString(requestData),
                            "sender", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.info("Room data sync broadcast sent: requestId={}, recordId={}", requestId, recordId.getValue());
            return requestId;

        } catch (Exception e) {
            log.error("Failed to broadcast room data sync", e);
            throw new RuntimeException("Failed to broadcast room data sync", e);
        }
    }

    /**
     * 완료 알림을 처리하여 대기 중인 Future를 완료시킴
     */
    public void handleCompletionNotification(String requestId, Object result) {
        CompletableFuture<Object> future = pendingRequests.remove(requestId);
        if (future != null && !future.isDone()) {
            // 에러 응답인지 확인
            if (result instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("error"))) {
                String message = (String) map.get("message");
                future.completeExceptionally(new RuntimeException(message));
                log.debug("Completed pending request with error: {} - {}", requestId, message);
            } else {
                future.complete(result);
                log.debug("Completed pending request: {}", requestId);
            }
        }
    }

    /**
     * 대기 중인 요청 수 반환 (모니터링용)
     */
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    private String getInstanceId() {
        return applicationName + "-" + serverPort;
    }
}
