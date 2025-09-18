package coffeeshout.room.infra.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventWaitManager {

    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingEvents = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> registerWait(String eventId) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        pendingEvents.put(eventId, (CompletableFuture<Object>) future);
        log.debug("방 이벤트 Future 등록: eventId={}", eventId);
        return future;
    }

    public <T> void notifySuccess(String eventId, T result) {
        final CompletableFuture<Object> future = pendingEvents.get(eventId);

        if (future == null) {
            log.warn("방 이벤트 성공 알림 실패: eventId={}에 대한 Future가 존재하지 않습니다.", eventId);
            return;
        }

        future.complete(result);
        log.debug("방 이벤트 성공 알림: eventId={}", eventId);
    }

    public void notifyFailure(String eventId, Throwable throwable) {
        final CompletableFuture<Object> future = pendingEvents.get(eventId);

        if (future == null) {
            log.warn("방 이벤트 실패 알림 시도: eventId={}에 해당하는 Future가 존재하지 않습니다.", eventId);
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
