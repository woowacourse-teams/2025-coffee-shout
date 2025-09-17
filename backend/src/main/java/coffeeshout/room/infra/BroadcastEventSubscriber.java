package coffeeshout.room.infra;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.event.ErrorBroadcastEvent;
import coffeeshout.room.ui.event.MiniGameUpdateBroadcastEvent;
import coffeeshout.room.ui.event.PlayerUpdateBroadcastEvent;
import coffeeshout.room.ui.event.WinnerAnnouncementBroadcastEvent;
import coffeeshout.room.ui.response.PlayerResponse;
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
public class BroadcastEventSubscriber implements MessageListener {

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ChannelTopic broadcastEventTopic;
    private final RoomService roomService;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(this, broadcastEventTopic);
        log.info("브로드캐스트 이벤트 구독 시작: topic={}", broadcastEventTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            final String body = new String(message.getBody());

            if (body.contains("\"broadcastEventType\":\"PLAYER_UPDATE\"")) {
                handlePlayerUpdateBroadcast(body);
                return;
            }

            if (body.contains("\"broadcastEventType\":\"MINI_GAME_UPDATE\"")) {
                handleMiniGameUpdateBroadcast(body);
                return;
            }

            if (body.contains("\"broadcastEventType\":\"WINNER_ANNOUNCEMENT\"")) {
                handleWinnerAnnouncementBroadcast(body);
                return;
            }

            if (body.contains("\"broadcastEventType\":\"ERROR\"")) {
                handleErrorBroadcast(body);
                return;
            }

            log.warn("알 수 없는 브로드캐스트 이벤트 타입: {}", body);
        } catch (final Exception e) {
            log.error("브로드캐스트 이벤트 처리 실패", e);
        }
    }

    private void handlePlayerUpdateBroadcast(String body) {
        try {
            final PlayerUpdateBroadcastEvent event = objectMapper.readValue(body, PlayerUpdateBroadcastEvent.class);

            log.info("플레이어 업데이트 브로드캐스트 수신: joinCode={}", event.joinCode());

            final List<PlayerResponse> responses = roomService.getAllPlayers(event.joinCode()).stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode(), WebSocketResponse.success(responses));

        } catch (final Exception e) {
            log.error("플레이어 업데이트 브로드캐스트 처리 실패", e);
        }
    }

    private void handleMiniGameUpdateBroadcast(String body) {
        try {
            final MiniGameUpdateBroadcastEvent event = objectMapper.readValue(body, MiniGameUpdateBroadcastEvent.class);

            log.info("미니게임 업데이트 브로드캐스트 수신: joinCode={}", event.joinCode());

            final List<MiniGameType> selectedMiniGames = roomService.getSelectedMiniGames(event.joinCode());

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/minigame",
                    WebSocketResponse.success(selectedMiniGames));

        } catch (final Exception e) {
            log.error("미니게임 업데이트 브로드캐스트 처리 실패", e);
        }
    }

    private void handleWinnerAnnouncementBroadcast(String body) {
        try {
            final WinnerAnnouncementBroadcastEvent event = objectMapper.readValue(body,
                    WinnerAnnouncementBroadcastEvent.class);

            log.info("당첨자 발표 브로드캐스트 수신: joinCode={}", event.joinCode());

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/winner",
                    WebSocketResponse.success(event.winner()));

        } catch (final Exception e) {
            log.error("당첨자 발표 브로드캐스트 처리 실패", e);
        }
    }

    private void handleErrorBroadcast(String body) {
        try {
            final ErrorBroadcastEvent event = objectMapper.readValue(body, ErrorBroadcastEvent.class);

            log.info("에러 브로드캐스트 수신: joinCode={}, destination={}", event.joinCode(), event.destination());

            messagingTemplate.convertAndSend(event.destination(),
                    WebSocketResponse.error(event.errorMessage()));

        } catch (final Exception e) {
            log.error("에러 브로드캐스트 처리 실패", e);
        }
    }
}
