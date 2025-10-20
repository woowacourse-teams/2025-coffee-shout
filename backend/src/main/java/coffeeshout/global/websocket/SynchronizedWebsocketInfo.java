package coffeeshout.global.websocket;

import org.springframework.messaging.Message;

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

}
