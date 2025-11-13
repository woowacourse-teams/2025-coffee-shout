package coffeeshout.cardgame.infra.messaging;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.RedisStreamPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamProducer {

    private final RedisStreamPublisher redisStreamPublisher;
    private final RedisStreamProperties redisStreamProperties;

    public void broadcastCardSelect(SelectCardCommandEvent event) {
        log.debug("카드 선택 이벤트 발송 준비: eventId={}, joinCode={}, playerName={}, cardIndex={}",
                event.eventId(), event.joinCode(), event.playerName(), event.cardIndex());

        redisStreamPublisher.publish(
                redisStreamProperties.cardGameSelectKey(),
                event,
                "카드 선택 이벤트"
        );

        log.debug("카드 선택 이벤트 발송 완료: eventId={}", event.eventId());
    }
}
