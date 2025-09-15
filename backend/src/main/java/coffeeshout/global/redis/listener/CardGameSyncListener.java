package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.minigame.CardSelectedEvent;
import coffeeshout.minigame.application.CardGameService;
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

    private final CardGameService cardGameService;
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
                // 다른 인스턴스에서 발생한 카드 선택을 로컬에 동기화
                // 주의: 이건 실제 게임 로직을 실행하지 않고 상태만 동기화
                log.debug("다른 인스턴스의 카드 선택 이벤트 수신: joinCode={}, player={}, cardIndex={}", 
                         event.joinCode(), event.playerName(), event.cardIndex());
                // TODO: 실제 동기화 로직 구현 필요
            }
        } catch (Exception e) {
            log.error("카드 선택 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
