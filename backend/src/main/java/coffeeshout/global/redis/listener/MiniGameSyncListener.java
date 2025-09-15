package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.minigame.MiniGameStartedEvent;
import coffeeshout.global.redis.event.minigame.MiniGamesUpdatedEvent;
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
public class MiniGameSyncListener implements MessageListener {

    private final MemoryRoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            switch (channel) {
                case "minigame:updated" -> handleMiniGamesUpdated(messageBody);
                case "minigame:started" -> handleMiniGameStarted(messageBody);
            }
        } catch (Exception e) {
            log.error("MiniGame 동기화 메시지 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleMiniGamesUpdated(String messageBody) {
        try {
            MiniGamesUpdatedEvent event = objectMapper.readValue(messageBody, MiniGamesUpdatedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncMiniGamesUpdated(event.joinCode(), event.miniGameTypes());
            
        } catch (Exception e) {
            log.error("미니게임 업데이트 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleMiniGameStarted(String messageBody) {
        try {
            MiniGameStartedEvent event = objectMapper.readValue(messageBody, MiniGameStartedEvent.class);
            
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            roomRepository.syncMiniGameStarted(
                event.joinCode(), 
                event.miniGameType(), 
                event.playerNames()
            );
            
            log.debug("미니게임 시작 동기화: joinCode={}, miniGameType={}", 
                     event.joinCode(), event.miniGameType());
            
        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
