package coffeeshout.global.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameDurationMetricService {

    private final MeterRegistry meterRegistry;
    private final Map<String, Sample> gameStartSamples = new ConcurrentHashMap<>();

    private Timer gameDurationTimer;

    public GameDurationMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initializeMetrics() {
        this.gameDurationTimer = Timer.builder("game.duration.time")
                .description("게임 진행 시간 (PLAYING → DONE)")
                .register(meterRegistry);
    }

    /**
     * 게임 시작 시점 기록 (RoomState: PLAYING)
     */
    public void startGameTimer(String joinCode) {
        Sample sample = Timer.start(meterRegistry);
        gameStartSamples.put(joinCode, sample);
        log.debug("게임 시작 타이머 시작: joinCode={}", joinCode);
    }

    /**
     * 게임 종료 시점 기록 (RoomState: DONE)
     */
    public void stopGameTimer(String joinCode) {
        Sample sample = gameStartSamples.remove(joinCode);
        if (sample != null) {
            long durationNanos = sample.stop(gameDurationTimer);
            double durationSeconds = durationNanos / 1_000_000_000.0;
            log.info("게임 완료: joinCode={}, duration={}초", joinCode, durationSeconds);
        } else {
            log.warn("게임 시작 샘플을 찾을 수 없음: joinCode={}", joinCode);
        }
    }

    /**
     * 게임이 중단된 경우 타이머 정리
     */
    public void cancelGameTimer(String roomId) {
        Sample sample = gameStartSamples.remove(roomId);
        if (sample != null) {
            log.debug("게임 타이머 취소: roomId={}", roomId);
        }
    }

    /**
     * 평균 게임 시간 조회 (초 단위)
     */
    public double getAverageGameDurationSeconds() {
        long count = gameDurationTimer.count();
        return count > 0 ? gameDurationTimer.totalTime(TimeUnit.SECONDS) / count : 0;
    }

    /**
     * 최대 게임 시간 조회 (초 단위)
     */
    public double getMaxGameDurationSeconds() {
        return gameDurationTimer.max(TimeUnit.SECONDS);
    }

    /**
     * 총 게임 횟수 조회
     */
    public long getTotalGameCount() {
        return gameDurationTimer.count();
    }
}
