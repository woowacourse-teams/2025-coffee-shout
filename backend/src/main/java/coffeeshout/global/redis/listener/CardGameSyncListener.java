package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.minigame.CardSelectedEvent;
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
public class CardGameSyncListener implements MessageListener {

    private final MemoryRoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            if (channel.equals("minigame:card:selected")) {
                handleCardSelectedEvent(messageBody);
            }
        } catch (Exception e) {
            log.error("카드 게임 동기화 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleCardSelectedEvent(String messageBody) {
        try {
            CardSelectedEvent event = objectMapper.readValue(messageBody, CardSelectedEvent.class);
            
            // 자신이 발행한 이벤트는 무시
            if (!event.instanceId().equals(instanceConfig.getInstanceId())) {
                roomRepository.syncCardSelected(
                    event.joinCode(),
                    event.playerName(),
                    event.cardIndex()
                );
                
                log.debug("카드 선택 동기화 완료: joinCode={}, player={}, cardIndex={}", 
                         event.joinCode(), event.playerName(), event.cardIndex());
            }
        } catch (Exception e) {
            log.error("카드 선택 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
