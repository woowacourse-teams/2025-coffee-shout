package coffeeshout.global.metric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class HttpMetricService {

    private final AtomicInteger concurrentRequests = new AtomicInteger(0);
    private final MeterRegistry meterRegistry;

    public HttpMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 실시간 동시 연결 수 게이지 등록
        Gauge.builder("http.concurrent.requests", concurrentRequests, AtomicInteger::get)
                .description("현재 동시 HTTP 처리 수")
                .register(meterRegistry);
    }

    public void incrementConcurrentRequests() {
        concurrentRequests.incrementAndGet();
    }

    public void decrementConcurrentRequests() {
        concurrentRequests.decrementAndGet();
    }
}
