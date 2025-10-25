package coffeeshout.global.exception;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(Exception.class)
    public void handleException(
            Exception e,
            @Header("simpUser") Principal user
    ) {

        messagingTemplate.convertAndSendToUser(
                user.getName(),
                "/queue/errors",
                WebSocketErrorResponse.from(e)
        );
    }
}
