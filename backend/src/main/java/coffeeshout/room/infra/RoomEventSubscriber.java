package coffeeshout.room.infra;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.WinnerResponse;
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

    private final LoggingSimpMessagingTemplate messagingTemplate;
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

            if (body.contains("\"eventType\":\"ROOM_CREATE\"")) {
                handleRoomCreateEvent(body);
                return;
            }

            if (body.contains("\"eventType\":\"ROOM_JOIN\"")) {
                handleRoomJoinEvent(body);
                return;
            }

            if (body.contains("\"eventType\":\"PLAYER_LIST_UPDATE\"")) {
                handlePlayerListUpdateEvent(body);
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

            if (body.contains("\"eventType\":\"ROULETTE_SPIN\"")) {
                handleRouletteSpinEvent(body);
                return;
            }

            log.warn("알 수 없는 이벤트 타입: {}", body);
        } catch (final Exception e) {
            log.error("이벤트 처리 실패", e);
        }
    }

    private void handleRoomCreateEvent(String body) {
        RoomCreateEvent event = null;
        try {
            event = objectMapper.readValue(body, RoomCreateEvent.class);

            log.info("방 생성 이벤트 수신: eventId={}, hostName={}, joinCode={}",
                    event.eventId(), event.hostName(), event.joinCode());

            final Room room = roomService.createRoomInternal(
                    event.hostName(),
                    event.selectedMenuRequest(),
                    event.joinCode()
            );

            // REST API 대기 중인 곳에 응답 알림
            roomEventWaitManager.notifySuccess(event.eventId(), room);

            log.info("방 생성 이벤트 처리 완료: eventId={}, joinCode={}", event.eventId(), event.joinCode());

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);

            if (event != null) {
                roomEventWaitManager.notifyFailure(event.eventId(), e);
            }
        }
    }

    private void handleRoomJoinEvent(String body) {
        RoomJoinEvent event = null;
        try {
            event = objectMapper.readValue(body, RoomJoinEvent.class);

            log.info("방 참가 이벤트 수신: eventId={}, joinCode={}, guestName={}",
                    event.eventId(), event.joinCode(), event.guestName());

            final Room room = roomService.enterRoomInternal(
                    event.joinCode(),
                    event.guestName(),
                    event.selectedMenuRequest()
            );

            // REST API 대기 중인 곳에 응답 알림
            roomEventWaitManager.notifySuccess(event.eventId(), room);

            log.info("방 참가 이벤트 처리 완료: eventId={}, joinCode={}, guestName={}",
                    event.eventId(), event.joinCode(), event.guestName());

        } catch (Exception e) {
            log.error("방 참가 이벤트 처리 실패", e);

            if (event != null) {
                roomEventWaitManager.notifyFailure(event.eventId(), e);
            }
        }
    }

    private void handlePlayerListUpdateEvent(String body) {
        try {
            final PlayerListUpdateEvent event = objectMapper.readValue(body, PlayerListUpdateEvent.class);

            log.info("플레이어 목록 업데이트 이벤트 수신: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

            final List<Player> players = roomService.getPlayersInternal(event.joinCode());
            final List<PlayerResponse> responses = players.stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode(),
                    WebSocketResponse.success(responses));

            log.info("플레이어 목록 업데이트 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

        } catch (final Exception e) {
            log.error("플레이어 목록 업데이트 이벤트 처리 실패", e);
        }
    }

    private void handlePlayerReadyEvent(String body) {
        PlayerReadyEvent event;
        try {
            event = objectMapper.readValue(body, PlayerReadyEvent.class);

            log.info("플레이어 ready 이벤트 수신: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

            final List<Player> players = roomService.changePlayerReadyStateInternal(
                    event.joinCode(),
                    event.playerName(),
                    event.isReady()
            );
            final List<PlayerResponse> responses = players.stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode(),
                    WebSocketResponse.success(responses));

            log.info("플레이어 ready 이벤트 처리 완료: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

        } catch (final Exception e) {
            log.error("플레이어 ready 이벤트 처리 실패", e);
        }
    }

    private void handleMiniGameSelectEvent(String body) {
        try {
            final MiniGameSelectEvent event = objectMapper.readValue(body, MiniGameSelectEvent.class);

            log.info("미니게임 선택 이벤트 수신: eventId={}, joinCode={}, hostName={}, miniGameTypes={}",
                    event.eventId(), event.joinCode(), event.hostName(), event.miniGameTypes());

            final List<MiniGameType> selectedMiniGames = roomService.updateMiniGamesInternal(
                    event.joinCode(),
                    event.hostName(),
                    event.miniGameTypes()
            );

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/minigame",
                    WebSocketResponse.success(selectedMiniGames));

            log.info("미니게임 선택 이벤트 처리 완료: eventId={}, joinCode={}, selectedCount={}",
                    event.eventId(), event.joinCode(), selectedMiniGames.size());

        } catch (final Exception e) {
            log.error("미니게임 선택 이벤트 처리 실패", e);
        }
    }

    private void handleRouletteSpinEvent(String body) {
        try {
            final RouletteSpinEvent event = objectMapper.readValue(body, RouletteSpinEvent.class);

            log.info("룰렛 스핀 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            final Winner winner = event.winner();
            final WinnerResponse response = WinnerResponse.from(winner);

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/winner",
                    WebSocketResponse.success(response));

            log.info("룰렛 스핀 이벤트 처리 완료: eventId={}, joinCode={}, winner={}",
                    event.eventId(), event.joinCode(), winner.name().value());

        } catch (final Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패", e);
        }
    }
}
