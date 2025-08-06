package coffeeshout.global.metric;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WebSocketMetricService {

    private final MeterRegistry meterRegistry;
    private final AtomicLong currentConnections = new AtomicLong(0);
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final Timer connectionEstablishmentTimer;
    private final Map<String, Timer.Sample> connectionSamples = new ConcurrentHashMap<>();

    public WebSocketMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("websocket.connections.current", currentConnections, AtomicLong::get)
                .description("현재 웹소켓 연결 개수")
                .register(meterRegistry);

        Gauge.builder("websocket.connections.total", totalConnections, AtomicLong::get)
                .description("총 웹소켓 연결 시도 개수")
                .register(meterRegistry);

        this.connectionEstablishmentTimer = Timer.builder("websocket.connection.establishment.time")
                .description("웹소켓 연결 수립 시간")
                .register(meterRegistry);
    }

    public void startConnection(String sessionId) {
        connectionSamples.put(sessionId, Timer.start(meterRegistry));
    }

    public void completeConnection(String sessionId) {
        currentConnections.incrementAndGet();
        totalConnections.incrementAndGet();

        Timer.Sample sample = connectionSamples.remove(sessionId);
        if (sample != null) {
            sample.stop(connectionEstablishmentTimer);
        }
    }

    public void failConnection(String sessionId, String reason) {
        connectionSamples.remove(sessionId); // 필요 없으니 제거
        Counter.builder("websocket.connections.failed")
                .tags("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordDisconnection(String sessionId, String reason, boolean isNormal) {
        currentConnections.decrementAndGet();
        connectionSamples.remove(sessionId); // 혹시 안 지워졌으면 정리

        Counter.builder("websocket.connections.disconnected")
                .tags("reason", reason, "type", isNormal ? "normal" : "abnormal")
                .register(meterRegistry)
                .increment();
    }
}
