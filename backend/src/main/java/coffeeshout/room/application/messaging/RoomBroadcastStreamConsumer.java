package coffeeshout.room.application.messaging;

import coffeeshout.global.messaging.RedisStreamBroadcastService;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomBroadcastStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final RedisStreamBroadcastService broadcastService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * StreamMessageListenerContainer에 리스너 등록
     */
    @PostConstruct
    public void registerListener() {
        // 단독 소비자 패턴으로 스트림 리스너 등록
        listenerContainer.receive(
                StreamOffset.fromStart(RedisStreamBroadcastService.BROADCAST_STREAM),
                this
        );

        log.info("Registered broadcast stream listener for: {}", RedisStreamBroadcastService.BROADCAST_STREAM);
    }

    /**
     * StreamListener 인터페이스 구현 - 메시지가 도착하면 자동 호출
     */
    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            log.info("Received broadcast message: id={}, value={}",
                    record.getId(), record.getValue());
            processBroadcastRecord(record);
        } catch (Exception e) {
            log.error("Failed to process broadcast message", e);
        }
    }

    private void processBroadcastRecord(MapRecord<String, String, String> record) {
        try {
            String requestId = record.getValue().get("requestId");
            String requestType = record.getValue().get("type");
            String requestDataJson = record.getValue().get("data");
            String sender = record.getValue().get("sender");

            log.info("Processing broadcast message: requestId={}, type={}, from={}",
                    requestId, requestType, sender);

            // 요청 처리
            Object result = switch (requestType) {
                case "ENTER_ROOM_BROADCAST" -> handleEnterRoomBroadcast(requestDataJson);
                default -> throw new IllegalArgumentException("Unknown broadcast type: " + requestType);
            };

            // 처리 결과를 응답 스트림에 전송
            broadcastService.sendBroadcastResponse(requestId, result);

            // 완료 알림을 별도 스트림으로 전송 (Producer의 콜백을 위함)
            sendCompletionNotification(requestId, result);

            log.info("Broadcast message processed: requestId={}, type={}", requestId, requestType);

        } catch (Exception e) {
            log.error("Failed to process broadcast record", e);

            // 에러 응답도 전송
            String requestId = record.getValue().get("requestId");
            Map<String, Object> errorResponse = Map.of(
                    "error", true,
                    "message", e.getMessage(),
                    "instanceId", getInstanceId()
            );
            broadcastService.sendBroadcastResponse(requestId, errorResponse);

            // 에러도 completion notification으로 전송
            sendCompletionNotification(requestId, errorResponse);
        }
    }

    @SuppressWarnings("unchecked")
    private Object handleEnterRoomBroadcast(String requestDataJson) throws Exception {
        Map<String, Object> requestData = objectMapper.readValue(requestDataJson, Map.class);
        String joinCode = (String) requestData.get("joinCode");
        String playerName = (String) requestData.get("playerName");
        SelectedMenuRequest selectedMenuRequest = objectMapper.convertValue(
                requestData.get("selectedMenuRequest"),
                SelectedMenuRequest.class
        );

        log.info("Handling enter room broadcast: joinCode={}, playerName={}", joinCode, playerName);

        roomService.enterRoom(joinCode, playerName, selectedMenuRequest);

        return Map.of(
                "status", "processed",
                "joinCode", joinCode,
                "playerName", playerName,
                "instanceId", getInstanceId()
        );
    }

    /**
     * 완료 알림을 별도 스트림으로 전송
     */
    private void sendCompletionNotification(String requestId, Object result) {
        try {
            RecordId recordId = stringRedisTemplate.opsForStream().add(
                    RoomBroadcastStreamProducer.COMPLETION_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "result", objectMapper.writeValueAsString(result),
                            "instanceId", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.debug("Completion notification sent: requestId={}, recordId={}", requestId, recordId.getValue());

        } catch (Exception e) {
            log.error("Failed to send completion notification: requestId={}", requestId, e);
        }
    }

    private String getInstanceId() {
        return applicationName + "-" + serverPort;
    }
}
