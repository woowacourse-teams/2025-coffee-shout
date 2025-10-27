package coffeeshout.global.websocket;

import java.security.Principal;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class SynchronizedWebsocketInfo {

    private static final ThreadLocal<Message<?>> threadLocal = new ThreadLocal<>();

    public static void bindWebsocketInfo(Message<?> message) {
        threadLocal.set(message);
    }

    public static void unbindWebsocketInfo() {
        threadLocal.remove();
    }

    public static Message<?> getWebsocketInfo() {
        return threadLocal.get();
    }

    public static String getUserName() {
        Message<?> message = getWebsocketInfo();
        if (message == null) {
            return "none";
        }
        Principal principal = getPrincipal();
        return principal != null ? principal.getName() : "none";
    }

    public static Principal getPrincipal() {
        Message<?> message = getWebsocketInfo();
        if (message == null) {
            return null;
        }
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                SimpMessageHeaderAccessor.class);
        return accessor != null ? accessor.getUser() : null;
    }
}
