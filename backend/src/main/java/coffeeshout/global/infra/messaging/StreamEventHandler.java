package coffeeshout.global.infra.messaging;

/**
 * Stream 이벤트 비즈니스 로직 처리
 * Consumer(메시징 인프라)와 Handler(비즈니스 로직)를 분리
 */
public interface StreamEventHandler<T> {

    void handle(T event) throws Exception;
}
