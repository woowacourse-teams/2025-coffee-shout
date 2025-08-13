package coffeeshout.global.interceptor.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

/**
 * STOMP 핸들러들을 관리하는 레지스트리
 */
@Slf4j
@Component
public class StompHandlerRegistry {

    private final Map<StompCommand, PreSendHandler> preSendHandlers;
    private final Map<StompCommand, PostSendHandler> postSendHandlers;

    public StompHandlerRegistry(List<PreSendHandler> preSendHandlers,
                                List<PostSendHandler> postSendHandlers) {
        this.preSendHandlers = preSendHandlers.stream()
                .collect(Collectors.toMap(PreSendHandler::getCommand, Function.identity()));

        this.postSendHandlers = postSendHandlers.stream()
                .collect(Collectors.toMap(PostSendHandler::getCommand, Function.identity()));

        log.info("STOMP Handler Registry 초기화 완료:");
        log.info("  - PreSend Handlers: {}", this.preSendHandlers.keySet());
        log.info("  - PostSend Handlers: {}", this.postSendHandlers.keySet());
    }

    /**
     * PreSend 핸들러 조회
     */
    public Optional<PreSendHandler> getPreSendHandler(StompCommand command) {
        return Optional.ofNullable(preSendHandlers.get(command));
    }

    /**
     * PostSend 핸들러 조회
     */
    public Optional<PostSendHandler> getPostSendHandler(StompCommand command) {
        return Optional.ofNullable(postSendHandlers.get(command));
    }
}