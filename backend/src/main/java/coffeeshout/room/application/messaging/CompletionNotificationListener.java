package coffeeshout.room.application.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompletionNotificationListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final RoomBroadcastStreamProducer roomBroadcastStreamProducer;
    private final ObjectMapper objectMapper;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    @PostConstruct
    public void registerListener() {
        // Completion 스트림 리스너 등록
        listenerContainer.receive(
                StreamOffset.latest(RoomBroadcastStreamProducer.COMPLETION_STREAM),
                this
        );

        log.info("Registered completion notification listener for: {}", RoomBroadcastStreamProducer.COMPLETION_STREAM);
    }

    @Async
    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            processCompletionNotification(record);
        } catch (Exception e) {
            log.error("Failed to process completion notification", e);
        }
    }

    private void processCompletionNotification(MapRecord<String, String, String> record) {
        try {
            String requestId = record.getValue().get("requestId");
            String resultJson = record.getValue().get("result");
            String instanceId = record.getValue().get("instanceId");

            log.debug("Processing completion notification: requestId={}, from={}", requestId, instanceId);

            // JSON 결과를 객체로 변환
            Object result = objectMapper.readValue(resultJson, Object.class);

            // Producer에게 완료 알림
            roomBroadcastStreamProducer.handleCompletionNotification(requestId, result);

            log.debug("Completion notification processed: requestId={}", requestId);

        } catch (Exception e) {
            log.error("Failed to process completion notification record", e);
        }
    }
}