package coffeeshout.room.infra;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
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
    private final RoomEventWaitManager roomEventWaitManager;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, roomEventTopic);
        log.info("방 이벤트 구독 시작: topic={}", roomEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            final String body = new String(message.getBody());
            
            if (body.contains("\"hostName\"")) {
                handleRoomCreateEvent(body);
                return;
            }
            
            if (body.contains("\"guestName\"")) {
                handleRoomJoinEvent(body);
                return;
            }
            
            log.warn("알 수 없는 이벤트 타입: {}", body);
        } catch (Exception e) {
            log.error("이벤트 처리 실패", e);
        }
    }
    
    private void handleRoomCreateEvent(String body) {
        RoomCreateEvent event = null;
        try {
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
            roomEventWaitManager.notifySuccess(event.getEventId(), room);

            log.info("방 생성 이벤트 처리 완료: eventId={}, joinCode={}", event.getEventId(), event.getJoinCode());

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
            
            if (event == null) {
                return;
            }
            
            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }
    
    private void handleRoomJoinEvent(String body) {
        RoomJoinEvent event = null;
        try {
            event = objectMapper.readValue(body, RoomJoinEvent.class);

            log.info("방 참가 이벤트 수신: eventId={}, joinCode={}, guestName={}",
                    event.getEventId(), event.getJoinCode(), event.getGuestName());

            // 모든 인스턴스가 동일하게 처리
            final Room room = roomService.enterRoomInternal(
                    event.getJoinCode(),
                    event.getGuestName(),
                    event.getSelectedMenuRequest()
            );

            // 방 참가 성공 알림
            roomEventWaitManager.notifySuccess(event.getEventId(), room);

            log.info("방 참가 이벤트 처리 완료: eventId={}, joinCode={}, guestName={}", 
                    event.getEventId(), event.getJoinCode(), event.getGuestName());

        } catch (Exception e) {
            log.error("방 참가 이벤트 처리 실패", e);
            
            if (event == null) {
                return;
            }
            
            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }
}
