package coffeeshout.room.infra;

import coffeeshout.room.domain.Room;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventWaitManager {

    private final ConcurrentHashMap<String, CompletableFuture<Room>> pendingEvents = new ConcurrentHashMap<>();

    public CompletableFuture<Room> registerWait(String eventId) {
        final CompletableFuture<Room> future = new CompletableFuture<>();
        pendingEvents.put(eventId, future);
        log.debug("방 이벤트 Future 등록: eventId={}", eventId);
        return future;
    }

    public Room waitForCompletion(String eventId, long timeoutSeconds) {
        final CompletableFuture<Room> future = pendingEvents.get(eventId);
        if (future == null) {
            log.warn("등록되지 않은 이벤트 대기 시도: eventId={}", eventId);
            return null;
        }

        try {
            final Room result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("방 이벤트 완료: eventId={}", eventId);
            return result;
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

    public void notifySuccess(String eventId, Room room) {
        final CompletableFuture<Room> future = pendingEvents.get(eventId);

        if (future == null) {
            return;
        }

        future.complete(room);
        log.debug("방 이벤트 성공 알림: eventId={}", eventId);
    }

    public void notifyFailure(String eventId, Throwable throwable) {
        final CompletableFuture<Room> future = pendingEvents.get(eventId);

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
