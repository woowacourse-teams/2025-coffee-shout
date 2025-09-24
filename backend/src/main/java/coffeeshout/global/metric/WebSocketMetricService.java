package coffeeshout.global.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketMetricService {

    private final MeterRegistry meterRegistry;
    private final AtomicLong currentConnections = new AtomicLong(0);
    private final Timer connectionEstablishmentTimer;
    private final Map<String, Sample> connectionSamples = new ConcurrentHashMap<>();

    // Counter 캐싱용
    private final Map<String, Counter> failedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> disconnectedCounters = new ConcurrentHashMap<>();

    public WebSocketMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("websocket.connections.current", currentConnections, AtomicLong::get)
                .description("현재 웹소켓 연결 개수")
                .register(meterRegistry);

        this.connectionEstablishmentTimer = Timer.builder("websocket.connection.establishment.time")
                .description("웹소켓 연결 수립 시간")
                .register(meterRegistry);
    }

    public void startConnection(String sessionId) {
        Sample sample = Timer.start(meterRegistry);
        connectionSamples.put(sessionId, sample);
    }

    public void completeConnection(String sessionId) {
        currentConnections.incrementAndGet();
        Sample sample = connectionSamples.remove(sessionId);
        if (sample != null) {
            long durationNanos = sample.stop(connectionEstablishmentTimer);
            double durationMs = durationNanos / 1_000_000.0;
            log.info("WebSocket 연결 수립 완료: sessionId={}, duration={}ms", sessionId, durationMs);
        }
    }

    public void failConnection(String sessionId, String reason) {
        connectionSamples.remove(sessionId);

        String key = "failed." + reason;
        Counter counter = failedCounters.computeIfAbsent(
                key, k ->
                        Counter.builder("websocket.connections.failed")
                                .description("웹소켓 연결 실패 건수")
                                .tag("reason", reason)
                                .register(meterRegistry)
        );
        counter.increment();
    }

    public void recordDisconnection(String sessionId, String reason) {
        connectionSamples.remove(sessionId);

        String key = "disconnected." + reason;

        Counter counter = disconnectedCounters.computeIfAbsent(
                key, k ->
                        Counter.builder("websocket.connections.disconnected")
                                .description("웹소켓 연결 해제 건수")
                                .tag("reason", reason)
                                .register(meterRegistry)
        );
        counter.increment();
        currentConnections.decrementAndGet();
    }

    // 평균 연결 수립 시간 조회
    public double getAverageConnectionTime() {
        long count = connectionEstablishmentTimer.count();
        return count > 0 ? connectionEstablishmentTimer.totalTime(TimeUnit.MILLISECONDS) / count : 0;
    }

    // 현재 연결 수 조회
    public long getCurrentConnections() {
        return currentConnections.get();
    }
}
