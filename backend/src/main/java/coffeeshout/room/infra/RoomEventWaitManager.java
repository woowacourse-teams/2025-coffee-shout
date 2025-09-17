package coffeeshout.room.infra;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventWaitManager {

    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingEvents = new ConcurrentHashMap<>();

    public <T> CompletableFuture<T> registerWait(String eventId) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        pendingEvents.put(eventId, (CompletableFuture<Object>) future);
        log.debug("방 이벤트 Future 등록: eventId={}", eventId);
        return future;
    }

    @SuppressWarnings("unchecked")
    public <T> T waitForCompletion(String eventId, long timeoutSeconds) {
        final CompletableFuture<Object> future = pendingEvents.get(eventId);
        if (future == null) {
            log.warn("등록되지 않은 이벤트 대기 시도: eventId={}", eventId);
            return null;
        }

        try {
            final Object result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("방 이벤트 완료: eventId={}", eventId);
            return (T) result;
        } catch (ExecutionException e) {
            log.error("방 이벤트 대기 실패: eventId={}", eventId, e);
            final Throwable cause = e.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            throw new RuntimeException(cause);
        } catch (Exception e) {
            log.error("방 이벤트 대기 실패: eventId={}", eventId, e);
            return null;
        } finally {
            pendingEvents.remove(eventId);
        }
    }

    public <T> void notifySuccess(String eventId, T result) {
        final CompletableFuture<Object> future = pendingEvents.get(eventId);

        if (future == null) {
            return;
        }

        future.complete(result);
        log.debug("방 이벤트 성공 알림: eventId={}", eventId);
    }

    public void notifyFailure(String eventId, Throwable throwable) {
        final CompletableFuture<Object> future = pendingEvents.get(eventId);

        if (future == null) {
            return;
        }

        future.completeExceptionally(throwable);
        log.debug("방 이벤트 실패 알림: eventId={}", eventId);
    }

    public void cleanup(String eventId) {
        pendingEvents.remove(eventId);
        log.debug("방 이벤트 정리: eventId={}", eventId);
    }
}
