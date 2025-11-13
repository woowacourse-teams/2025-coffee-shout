package coffeeshout.global.config.redis;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.cardgame.infra.messaging.CardSelectEventHandler;
import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.GenericStreamConsumer;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.infra.messaging.RoomJoinEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

/**
 * Redis Stream Consumer 설정
 * <p>
 * {@link GenericStreamConsumer}를 사용하여 각 도메인 이벤트별로 Consumer를 등록합니다.
 * Consumer는 메시징 인프라를 담당하고, Handler는 비즈니스 로직을 담당합니다.
 * </p>
 *
 * <p><b>등록된 Consumer:</b></p>
 * <ul>
 *   <li>roomJoinConsumer - 방 입장 이벤트 처리</li>
 *   <li>cardSelectConsumer - 카드 선택 이벤트 처리</li>
 * </ul>
 *
 * @see GenericStreamConsumer
 * @see RoomJoinEventHandler
 * @see CardSelectEventHandler
 */
@Configuration
public class StreamConsumerConfig {

    /**
     * 방 입장 이벤트를 처리하는 Consumer를 등록합니다.
     * <p>
     * RoomJoinEvent를 구독하여 게스트의 방 입장을 처리합니다.
     * 순서 보장을 위해 단일 스레드로 동작합니다.
     * </p>
     *
     * @param handler                 방 입장 이벤트 Handler
     * @param roomEnterStreamContainer 방 입장 전용 Stream 컨테이너
     * @param properties              Redis Stream 설정 Properties
     * @param objectMapper            JSON 변환용 ObjectMapper
     * @return 방 입장 이벤트 Consumer
     */
    @Bean
    public GenericStreamConsumer<RoomJoinEvent> roomJoinConsumer(
            RoomJoinEventHandler handler,
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

    /**
     * 카드 선택 이벤트를 처리하는 Consumer를 등록합니다.
     * <p>
     * SelectCardCommandEvent를 구독하여 플레이어의 카드 선택을 처리합니다.
     * 순서 보장을 위해 단일 스레드로 동작합니다.
     * </p>
     *
     * @param handler                  카드 선택 이벤트 Handler
     * @param cardSelectStreamContainer 카드 선택 전용 Stream 컨테이너
     * @param properties               Redis Stream 설정 Properties
     * @param objectMapper             JSON 변환용 ObjectMapper
     * @return 카드 선택 이벤트 Consumer
     */
    @Bean
    public GenericStreamConsumer<SelectCardCommandEvent> cardSelectConsumer(
            CardSelectEventHandler handler,
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
