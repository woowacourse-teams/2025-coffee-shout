package coffeeshout.global.config.redis;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.cardgame.infra.messaging.CardSelectStreamHandler;
import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.GenericStreamConsumer;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.infra.messaging.RoomJoinStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@Configuration
public class StreamConsumerConfig {

    @Bean
    public GenericStreamConsumer<RoomJoinEvent> roomJoinConsumer(
            RoomJoinStreamHandler handler,
            @Qualifier("roomEnterStreamContainer") StreamMessageListenerContainer<String, ObjectRecord<String, String>> roomEnterStreamContainer,
            RedisStreamProperties properties,
            ObjectMapper objectMapper
    ) {
        return new GenericStreamConsumer<>(
                handler,
                RoomJoinEvent.class,
                roomEnterStreamContainer,
                properties.roomJoinKey(),
                "방 입장",
                objectMapper
        );
    }

    @Bean
    public GenericStreamConsumer<SelectCardCommandEvent> cardSelectConsumer(
            CardSelectStreamHandler handler,
            @Qualifier("cardSelectStreamContainer") StreamMessageListenerContainer<String, ObjectRecord<String, String>> cardSelectStreamContainer,
            RedisStreamProperties properties,
            ObjectMapper objectMapper
    ) {
        return new GenericStreamConsumer<>(
                handler,
                SelectCardCommandEvent.class,
                cardSelectStreamContainer,
                properties.cardGameSelectKey(),
                "카드 선택",
                objectMapper
        );
    }
}
