package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.player.HostPromotedEvent;
import coffeeshout.global.redis.event.player.PlayerJoinedEvent;
import coffeeshout.global.redis.event.player.PlayerMenuSelectedEvent;
import coffeeshout.global.redis.event.player.PlayerReadyStateChangedEvent;
import coffeeshout.global.redis.event.player.PlayerRemovedEvent;
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
public class PlayerSyncListener implements MessageListener {

    private final MemoryRoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            switch (channel) {
                case "player:joined" -> handlePlayerJoined(messageBody);
                case "player:removed" -> handlePlayerRemoved(messageBody);
                case "player:menu" -> handlePlayerMenuSelected(messageBody);
                case "player:ready" -> handlePlayerReadyStateChanged(messageBody);
                case "player:host" -> handleHostPromoted(messageBody);
            }
        } catch (Exception e) {
            log.error("Player 동기화 메시지 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handlePlayerJoined(String messageBody) {
        try {
            PlayerJoinedEvent event = objectMapper.readValue(messageBody, PlayerJoinedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncPlayerJoined(
                event.joinCode(),
                event.playerName(),
                event.playerType(),
                event.selectedMenu(),
                event.isReady(),
                event.colorIndex()
            );
            
        } catch (Exception e) {
            log.error("플레이어 입장 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handlePlayerRemoved(String messageBody) {
        try {
            PlayerRemovedEvent event = objectMapper.readValue(messageBody, PlayerRemovedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncPlayerRemoved(event.joinCode(), event.playerName());
            
        } catch (Exception e) {
            log.error("플레이어 제거 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handlePlayerMenuSelected(String messageBody) {
        try {
            PlayerMenuSelectedEvent event = objectMapper.readValue(messageBody, PlayerMenuSelectedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncPlayerMenuSelected(
                event.joinCode(),
                event.playerName(),
                event.selectedMenu()
            );
            
        } catch (Exception e) {
            log.error("플레이어 메뉴 선택 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handlePlayerReadyStateChanged(String messageBody) {
        try {
            PlayerReadyStateChangedEvent event = objectMapper.readValue(messageBody, PlayerReadyStateChangedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncPlayerReadyState(
                event.joinCode(),
                event.playerName(),
                event.isReady()
            );
            
        } catch (Exception e) {
            log.error("플레이어 준비 상태 변경 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleHostPromoted(String messageBody) {
        try {
            HostPromotedEvent event = objectMapper.readValue(messageBody, HostPromotedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncHostPromoted(event.joinCode(), event.newHostName());
            
        } catch (Exception e) {
            log.error("호스트 승격 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
