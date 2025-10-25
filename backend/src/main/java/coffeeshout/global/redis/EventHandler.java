package coffeeshout.global.redis;

public interface EventHandler {

    void handle(BaseEvent event);

    Class<?> eventType();
}
