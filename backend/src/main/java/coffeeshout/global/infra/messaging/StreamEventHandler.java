package coffeeshout.global.infra.messaging;

/**
 * Redis Stream 이벤트를 처리하는 Handler 인터페이스
 * <p>
 * 비즈니스 로직을 메시징 인프라(Consumer)로부터 분리하기 위한 인터페이스입니다.
 * 각 도메인별로 이 인터페이스를 구현하여 이벤트 처리 로직을 작성합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *   <li>도메인 이벤트 처리 비즈니스 로직 구현</li>
 *   <li>도메인 서비스 호출 및 조율</li>
 *   <li>비즈니스 예외 처리</li>
 * </ul>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>
 * &#64;Component
 * public class RoomJoinEventHandler implements StreamEventHandler&lt;RoomJoinEvent&gt; {
 *
 *     private final RoomCommandService roomCommandService;
 *
 *     &#64;Override
 *     public void handle(RoomJoinEvent event) {
 *         // 비즈니스 로직 구현
 *         roomCommandService.joinGuest(...);
 *     }
 * }
 * </pre>
 *
 * @param <T> 처리할 이벤트 타입
 * @see GenericStreamConsumer
 */
public interface StreamEventHandler<T> {

    /**
     * 이벤트를 처리합니다.
     * <p>
     * 비즈니스 로직을 수행하고, 필요한 경우 예외를 던집니다.
     * 던져진 예외는 {@link GenericStreamConsumer}에서 처리됩니다.
     * </p>
     *
     * @param event 처리할 이벤트
     * @throws Exception 비즈니스 로직 수행 중 발생한 예외
     */
    void handle(T event) throws Exception;
}
