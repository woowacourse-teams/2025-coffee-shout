package coffeeshout.global.config.redis;

import lombok.Getter;
import org.springframework.data.redis.listener.ChannelTopic;

@Getter
public enum EventTopicRegistry {
    ROOM("room.events"),
    MINI_GAME("minigame.events"),
    PLAYER("player.events"),
    SESSION("session.events"),
    RACING_GAME("racinggame.events");

    private final String topicName;

    EventTopicRegistry(String topicName) {
        this.topicName = topicName;
    }

    public ChannelTopic toChannelTopic() {
        return new ChannelTopic(topicName);
    }
}
