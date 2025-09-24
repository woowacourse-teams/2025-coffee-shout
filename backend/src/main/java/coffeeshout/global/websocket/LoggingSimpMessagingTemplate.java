package coffeeshout.global.websocket;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingSimpMessagingTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    @Observed(name = "websocket.send")
    public void convertAndSend(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}
