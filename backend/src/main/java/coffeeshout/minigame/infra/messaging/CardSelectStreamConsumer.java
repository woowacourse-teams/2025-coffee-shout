package coffeeshout.minigame.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.message.RedisStreamStartStrategy;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.cardgame.event.SelectCardCommandEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {

    private final CardGameService cardGameService;
    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> objectRecordStreamMessageListenerContainer;
    private final RedisStreamStartStrategy redisStreamStartStrategy;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void registerListener() {
        // 단독 소비자 패턴으로 스트림 리스너 등록
        objectRecordStreamMessageListenerContainer.receive(
                redisStreamStartStrategy.getStreamOffset(redisStreamProperties.cardGameSelectKey()),
                this
        );

        log.info("Registered broadcast stream listener for: {}", redisStreamProperties.cardGameSelectKey());
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        try {
            String jsonValue = message.getValue();
            String value = objectMapper.readValue(jsonValue, String.class);
            SelectCardCommandEvent event = objectMapper.readValue(value, SelectCardCommandEvent.class);

            log.info("Received card select message: id={}, event={}",
                    message.getId(), event);

            cardGameService.selectCard(event.joinCode(), event.playerName(), event.cardIndex());
        } catch (Exception e) {
            log.error("Failed to process card select message: {}", message, e);
        }
    }
}
