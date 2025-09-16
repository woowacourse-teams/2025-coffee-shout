package coffeeshout.room.infra;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventSubscriber implements MessageListener {

    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic roomEventTopic;
    private final RoomCreationWaitManager roomCreationWaitManager;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, roomEventTopic);
        log.info("방 이벤트 구독 시작: topic={}", roomEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RoomCreateEvent event = null;
        try {
            final String body = new String(message.getBody());
            event = objectMapper.readValue(body, RoomCreateEvent.class);

            log.info("방 생성 이벤트 수신: eventId={}, hostName={}, joinCode={}",
                    event.getEventId(), event.getHostName(), event.getJoinCode());

            // 모든 인스턴스가 동일하게 처리 (자신이 발행한 것도 포함)
            final Room room = roomService.createRoomInternal(
                    event.getHostName(),
                    event.getSelectedMenuRequest(),
                    event.getJoinCode()
            );

            // 방 생성 성공 알림
            roomCreationWaitManager.notifySuccess(event.getEventId(), room);

            log.info("방 생성 이벤트 처리 완료: eventId={}, joinCode={}", event.getEventId(), event.getJoinCode());

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
            handleEventFailure(message, event, e);
        }
    }
    
    private void handleEventFailure(Message message, RoomCreateEvent event, Exception e) {
        if (event != null) {
            roomCreationWaitManager.notifyFailure(event.getEventId(), e);
            return;
        }
        
        // 이벤트 파싱 실패한 경우
        try {
            final String body = new String(message.getBody());
            final RoomCreateEvent failedEvent = objectMapper.readValue(body, RoomCreateEvent.class);
            roomCreationWaitManager.notifyFailure(failedEvent.getEventId(), e);
        } catch (Exception ex) {
            log.error("실패한 이벤트의 Future 처리 실패", ex);
        }
    }
}
