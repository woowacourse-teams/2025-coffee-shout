package coffeeshout.room.domain.event;

/**
 * 도메인 이벤트 발행 인터페이스
 * <p>
 * - 도메인/애플리케이션 계층에서 정의하는 추상화
 * - 구체적인 메시징 기술(Redis, Kafka 등)에 독립적
 * - 의존성 역전 원칙(DIP)을 통해 Application Layer가 Infra Layer에 의존하지 않도록 함
 * </p>
 *
 * <p><b>설계 원칙:</b></p>
 * <ul>
 *   <li>Application Layer는 이 인터페이스에만 의존</li>
 *   <li>Infra Layer에서 구현 (RedisEventPublisher 등)</li>
 *   <li>이벤트 발행 방식(Pub/Sub, Stream 등)은 구현체가 결정</li>
 * </ul>
 */
public interface EventPublisher {

    /**
     * 이벤트를 발행한다
     * <p>
     * 이벤트 타입에 따라 적절한 메커니즘으로 발행됨:
     * <ul>
     *   <li>RoomJoinEvent: Redis Stream (순서 보장 필요)</li>
     *   <li>기타 이벤트: Redis Pub/Sub (브로드캐스트)</li>
     * </ul>
     * </p>
     *
     * @param event 발행할 이벤트
     * @param <T> 이벤트 타입 (RoomBaseEvent를 구현한 타입)
     * @throws RuntimeException 이벤트 발행 실패 시
     */
    <T extends RoomBaseEvent> void publish(T event);
}
