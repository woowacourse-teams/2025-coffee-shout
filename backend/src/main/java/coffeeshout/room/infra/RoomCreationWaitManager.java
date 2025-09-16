package coffeeshout.room.infra;

import coffeeshout.room.domain.Room;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomCreationWaitManager {
    
    private final ConcurrentHashMap<String, CompletableFuture<Room>> pendingCreations = new ConcurrentHashMap<>();
    
    public CompletableFuture<Room> registerWait(String eventId) {
        final CompletableFuture<Room> future = new CompletableFuture<>();
        pendingCreations.put(eventId, future);
        log.debug("방 생성 Future 등록: eventId={}", eventId);
        return future;
    }
    
    public Room waitForCompletion(String eventId, long timeoutSeconds) {
        final CompletableFuture<Room> future = pendingCreations.get(eventId);
        if (future == null) {
            log.warn("등록되지 않은 이벤트 대기 시도: eventId={}", eventId);
            return null;
        }
        
        try {
            final Room result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("방 생성 완료: eventId={}", eventId);
            return result;
        } catch (Exception e) {
            log.error("방 생성 대기 실패: eventId={}", eventId, e);
            return null;
        } finally {
            pendingCreations.remove(eventId);
        }
    }
    
    public void notifySuccess(String eventId, Room room) {
        final CompletableFuture<Room> future = pendingCreations.get(eventId);
        if (future != null) {
            future.complete(room);
            log.debug("방 생성 성공 알림: eventId={}", eventId);
        }
    }
    
    public void notifyFailure(String eventId, Throwable throwable) {
        final CompletableFuture<Room> future = pendingCreations.get(eventId);
        if (future != null) {
            future.completeExceptionally(throwable);
            log.debug("방 생성 실패 알림: eventId={}", eventId);
        }
    }
}
