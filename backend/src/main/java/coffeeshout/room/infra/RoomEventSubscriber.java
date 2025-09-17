package coffeeshout.room.infra;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.player.Player;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
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
    public void onMessage(final Message message, final byte[] pattern) {
        try {
            final String body = new String(message.getBody());

            if (body.contains("\"eventType\":\"ROOM_CREATE\"")) {
                handleRoomCreateEvent(body);
                return;
            }

            if (body.contains("\"eventType\":\"ROOM_JOIN\"")) {
                handleRoomJoinEvent(body);
                return;
            }

            if (body.contains("\"eventType\":\"PLAYER_READY\"")) {
                handlePlayerReadyEvent(body);
                return;
            }

            if (body.contains("\"eventType\":\"MINI_GAME_SELECT\"")) {
                handleMiniGameSelectEvent(body);
                return;
            }

            log.warn("알 수 없는 이벤트 타입: {}", body);
        } catch (final Exception e) {
            log.error("이벤트 처리 실패", e);
        }
    }

    private void handleRoomCreateEvent(final String body) {
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

        } catch (final Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);

            if (event == null) {
                return;
            }

            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    private void handleRoomJoinEvent(final String body) {
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

        } catch (final Exception e) {
            log.error("방 참가 이벤트 처리 실패", e);

            if (event == null) {
                return;
            }

            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    private void handlePlayerReadyEvent(final String body) {
        PlayerReadyEvent event = null;
        try {
            event = objectMapper.readValue(body, PlayerReadyEvent.class);

            log.info("플레이어 ready 이벤트 수신: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.getEventId(), event.getJoinCode(), event.getPlayerName(), event.getIsReady());

            // 모든 인스턴스가 동일하게 처리
            final List<Player> players = roomService.changePlayerReadyStateInternal(
                    event.getJoinCode(),
                    event.getPlayerName(),
                    event.getIsReady()
            );

            // ready 상태 변경 성공 알림
            roomEventWaitManager.notifySuccess(event.getEventId(), players);

            log.info("플레이어 ready 이벤트 처리 완료: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.getEventId(), event.getJoinCode(), event.getPlayerName(), event.getIsReady());

        } catch (final Exception e) {
            log.error("플레이어 ready 이벤트 처리 실패", e);

            if (event == null) {
                return;
            }

            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    private void handleMiniGameSelectEvent(final String body) {
        MiniGameSelectEvent event = null;
        try {
            event = objectMapper.readValue(body, MiniGameSelectEvent.class);

            log.info("미니게임 선택 이벤트 수신: eventId={}, joinCode={}, hostName={}, miniGameTypes={}",
                    event.getEventId(), event.getJoinCode(), event.getHostName(), event.getMiniGameTypes());

            // 모든 인스턴스가 동일하게 처리
            final List<MiniGameType> selectedMiniGames = roomService.updateMiniGamesInternal(
                    event.getJoinCode(),
                    event.getHostName(),
                    event.getMiniGameTypes()
            );

            // 미니게임 선택 성공 알림
            roomEventWaitManager.notifySuccess(event.getEventId(), selectedMiniGames);

            log.info("미니게임 선택 이벤트 처리 완료: eventId={}, joinCode={}, selectedCount={}",
                    event.getEventId(), event.getJoinCode(), selectedMiniGames.size());

        } catch (final Exception e) {
            log.error("미니게임 선택 이벤트 처리 실패", e);

            if (event == null) {
                return;
            }

            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }
}
