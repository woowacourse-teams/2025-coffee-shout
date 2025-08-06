package coffeeshout.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    public void convertAndSend(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}
