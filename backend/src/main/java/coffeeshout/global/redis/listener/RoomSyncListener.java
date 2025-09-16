package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.room.RoomCreatedEvent;
import coffeeshout.global.redis.event.room.RoomDeletedEvent;
import coffeeshout.room.domain.repository.MemoryRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomSyncListener implements MessageListener {

    private final MemoryRoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            switch (channel) {
                case "room:created" -> handleRoomCreated(messageBody);
                case "room:deleted" -> handleRoomDeleted(messageBody);
            }
        } catch (Exception e) {
            log.error("Room 동기화 메시지 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleRoomCreated(String messageBody) {
        try {
            RoomCreatedEvent event = objectMapper.readValue(messageBody, RoomCreatedEvent.class);
            
            // 자기가 발행한 이벤트는 무시
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncRoomCreated(
                event.joinCode(),
                event.hostName(),
                event.hostMenu(),
                event.qrCodeUrl()
            );
            
        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleRoomDeleted(String messageBody) {
        try {
            RoomDeletedEvent event = objectMapper.readValue(messageBody, RoomDeletedEvent.class);
            
            // 자기가 발행한 이벤트는 무시
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncRoomDeleted(event.joinCode());
            
        } catch (Exception e) {
            log.error("방 삭제 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
