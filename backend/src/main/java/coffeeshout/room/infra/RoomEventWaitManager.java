package coffeeshout.room.infra;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventWaitManager {

    private final Map<String, CompletableFuture<Object>> waitingFutures = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> registerWait(String eventId) {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        waitingFutures.put(eventId, future);
        log.debug("이벤트 대기 등록: eventId={}", eventId);
        return (CompletableFuture<T>) future;
    }

    public void notifySuccess(String eventId, Object result) {
        final CompletableFuture<Object> future = waitingFutures.remove(eventId);
        if (future != null) {
            future.complete(result);
            log.debug("이벤트 처리 성공 알림: eventId={}", eventId);
        }
    }

    public void notifyFailure(String eventId, Throwable throwable) {
        final CompletableFuture<Object> future = waitingFutures.remove(eventId);
        if (future != null) {
            future.completeExceptionally(throwable);
            log.debug("이벤트 처리 실패 알림: eventId={}", eventId);
        }
    }

    public void cleanup(String eventId) {
        waitingFutures.remove(eventId);
        log.debug("이벤트 대기 정리: eventId={}", eventId);
    }
}
