package coffeeshout.global.redis.pubsub;

import coffeeshout.global.redis.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PubSubPublishManager {

    private final PubSubEventPublisher pubSubEventPublisher;

    public void publishRoom(BaseEvent event) {
        pubSubEventPublisher.publishEvent(event, PubSubChannelTopic.ROOM.convert());
    }

    public void publishMiniGame(BaseEvent event) {
        pubSubEventPublisher.publishEvent(event, PubSubChannelTopic.MINIGAME.convert());
    }

    public void publishPlayer(BaseEvent event) {
        pubSubEventPublisher.publishEvent(event, PubSubChannelTopic.PLAYER.convert());
    }

    public void publishSession(BaseEvent event) {
        pubSubEventPublisher.publishEvent(event, PubSubChannelTopic.SESSION.convert());
    }

    public void publishRacingGame(BaseEvent event) {
        pubSubEventPublisher.publishEvent(event, PubSubChannelTopic.RACING_GAME.convert());
    }
}
