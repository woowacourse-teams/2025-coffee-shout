package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.websocket.WebSocketBroadcastEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSyncListener implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            if ("websocket:broadcast".equals(channel)) {
                handleWebSocketBroadcast(messageBody);
            }
        } catch (Exception e) {
            log.error("WebSocket 동기화 메시지 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleWebSocketBroadcast(String messageBody) {
        try {
            WebSocketBroadcastEvent event = objectMapper.readValue(messageBody, WebSocketBroadcastEvent.class);
            
            // 자기가 발행한 이벤트는 무시 (이미 로컬에서 전송했음)
            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }
            
            // 다른 인스턴스에서 온 웹소켓 메시지를 로컬 클라이언트들에게 전송
            messagingTemplate.convertAndSend(event.destination(), event.payload());
            
            log.debug("웹소켓 메시지 동기화 완료: destination={}", event.destination());
            
        } catch (Exception e) {
            log.error("웹소켓 브로드캐스트 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
