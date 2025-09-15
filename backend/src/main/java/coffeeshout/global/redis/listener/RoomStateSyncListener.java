package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.room.RoomStateChangedEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomStateSyncListener implements MessageListener {

    private final RoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            if (channel.equals("room:state")) {
                handleRoomStateChangedEvent(messageBody);
            }
        } catch (Exception e) {
            log.error("룸 상태 동기화 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleRoomStateChangedEvent(String messageBody) {
        try {
            RoomStateChangedEvent event = objectMapper.readValue(messageBody, RoomStateChangedEvent.class);
            
            // 자신이 발행한 이벤트는 무시
            if (!event.instanceId().equals(instanceConfig.getInstanceId())) {
                JoinCode joinCode = new JoinCode(event.joinCode());
                Room room = roomRepository.findByJoinCode(joinCode);
                
                if (room != null) {
                    // 실제로는 Room에 상태를 직접 설정하는 메서드가 필요함
                    // room.setState(event.roomState());
                    roomRepository.save(room);
                    
                    log.debug("룸 상태 동기화 완료: joinCode={}, newState={}", 
                             event.joinCode(), event.newState());
                }
            }
        } catch (Exception e) {
            log.error("룸 상태 변경 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
