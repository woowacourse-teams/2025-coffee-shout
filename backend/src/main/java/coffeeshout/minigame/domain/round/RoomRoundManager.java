package coffeeshout.minigame.domain.round;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

/**
 * 특정 방(Room)의 라운드 관리를 담당하는 매니저
 * 각 방마다 독립적인 인스턴스가 생성됩니다.
 */
@Slf4j
public class RoomRoundManager {

    /**
     * -- GETTER --
     *  관리하는 방의 JoinCode를 반환합니다.
     */
    @Getter
    private final JoinCode joinCode;
    private final Map<RoundPhase, RoundPhaseHandler> handlers;
    private final TaskScheduler scheduler;
    private final AtomicReference<ScheduledFuture<?>> currentTask = new AtomicReference<>();
    /**
     * -- GETTER --
     *  현재 활성 상태인지 확인합니다.
     */
    @Getter
    private volatile boolean isActive = true;
    
    public RoomRoundManager(JoinCode joinCode, List<RoundPhaseHandler> handlerList, TaskScheduler scheduler) {
        this.joinCode = joinCode;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                    RoundPhaseHandler::getPhase,
                    Function.identity()
                ));
        this.scheduler = scheduler;
        
        log.info("방 {} 전용 RoundManager 생성 완료", joinCode.value());
    }
    
    /**
     * 현재 단계를 실행하고 다음 단계를 스케줄링합니다.
     */
    public void executePhase(CardGame game, Room room, Runnable onStateChange) {
        if (!isActive) {
            log.warn("비활성화된 RoundManager에서 실행 시도: {}", joinCode.value());
            return;
        }
        
        RoundState currentState = game.getRoundState();
        log.info(currentState.phase().name());
        RoundPhaseHandler handler = handlers.get(currentState.phase());

        if (handler == null) {
            log.error("핸들러를 찾을 수 없음: {} (방: {})", currentState.phase(), joinCode.value());
            return;
        }
        
        log.info("방 {} - 단계 실행: {}", joinCode.value(), currentState);
        
        // 현재 단계 실행
        try {
            handler.execute(game, room);
            onStateChange.run(); // 상태 변경 알림
            
            // 게임이 완료되었으면 더 이상 스케줄링하지 않음
            if (currentState.isGameFinished()) {
                log.info("방 {} - 게임 완료", joinCode.value());
                cleanup();
                return;
            }
            
            // 다음 단계 스케줄링
            scheduleNextPhase(game, room, handler, onStateChange);
            
        } catch (Exception e) {
            log.error("방 {} - 단계 실행 중 오류 발생: {}", joinCode.value(), currentState, e);
        }
    }
    
    private void scheduleNextPhase(CardGame game, Room room, 
                                 RoundPhaseHandler handler, Runnable onStateChange) {
        
        if (!isActive) {
            return;
        }

        Duration duration = handler.getDuration();
        if (!duration.isZero()) {
            log.info("방 {} - 다음 단계까지 {}초 대기", joinCode.value(), duration.toSeconds());

            ScheduledFuture<?> future = scheduler.schedule(
                    () -> {
                        if (isActive) {
                            moveToNextPhase(game, room, onStateChange);
                        }
                    },
                    Instant.now().plus(duration)
            );

            // 기존 스케줄된 작업이 있다면 취소
            cancelCurrentTask();
            currentTask.set(future);
        } else {
            // 지속 시간이 0이면 즉시 다음 단계로
            moveToNextPhase(game, room, onStateChange);
        }
    }
    
    private void moveToNextPhase(CardGame game, Room room, Runnable onStateChange) {
        if (!isActive) {
            return;
        }
        
        RoundState nextState = game.getRoundState().nextPhase(game.getMaxRounds());
        game.setRoundState(nextState);
        
        log.info("방 {} - 다음 단계로 이동: {}", joinCode.value(), nextState);
        
        // 다음 단계 실행
        executePhase(game, room, onStateChange);
    }
    
    /**
     * 현재 스케줄된 작업을 취소합니다.
     */
    public void cancelCurrentTask() {
        ScheduledFuture<?> future = currentTask.getAndSet(null);
        if (future != null && !future.isDone()) {
            future.cancel(false);
            log.info("방 {} - 스케줄된 작업 취소", joinCode.value());
        }
    }
    
    /**
     * 이 RoundManager를 정리합니다. (방 종료 시 호출)
     */
    public void cleanup() {
        log.info("방 {} RoundManager 정리 시작", joinCode.value());
        
        isActive = false;
        cancelCurrentTask();
        
        log.info("방 {} RoundManager 정리 완료", joinCode.value());
    }

}
