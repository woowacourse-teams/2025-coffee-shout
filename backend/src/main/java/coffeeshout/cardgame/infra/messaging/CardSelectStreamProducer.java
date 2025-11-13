package coffeeshout.cardgame.infra.messaging;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.infra.messaging.RedisStreamPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CardGame 도메인 카드 선택 이벤트를 Redis Stream으로 발행하는 Producer
 * <p>
 * {@link RedisStreamPublisher}를 합성(composition)하여 공통 발행 로직을 재사용합니다.
 * 이 클래스는 CardGame 도메인에 특화된 로깅과 Stream 키 설정을 담당합니다.
 * </p>
 *
 * @see RedisStreamPublisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamProducer {

    private final RedisStreamPublisher redisStreamPublisher;
    private final RedisStreamProperties redisStreamProperties;

    /**
     * 카드 선택 이벤트를 Redis Stream으로 브로드캐스트합니다.
     *
     * @param event 카드 선택 명령 이벤트
     * @throws RuntimeException Stream 발행 실패 시
     */
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
