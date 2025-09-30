package coffeeshout.global.event;

import coffeeshout.global.config.trace.ObservationRegistryProvider;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis publish를 위한 이벤트들의 기본 클래스
 * tracing 정보(traceId, spanId)를 포함하여 분산 추적 지원
 */
@Slf4j
@Getter
public abstract class BaseEvent {

    public final String eventId;
    public final TraceInfo traceInfo;
    public final Instant timestamp;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.traceInfo = extractTraceInfo();
    }

    private TraceInfo extractTraceInfo() {
        try {
            final ObservationRegistry observationRegistry = ObservationRegistryProvider.getObservationRegistry();
            final Observation observation = observationRegistry.getCurrentObservation();
            final TracingContext traceContext = observation.getContext().get(TracingContext.class);
            return new TraceInfo(
                    traceContext.getSpan().context().traceId(),
                    traceContext.getSpan().context().spanId()
            );
        } catch (Exception e) {
            log.debug("Trace context 없음: {}", e.toString());
            return new TraceInfo("", "");
        }
    }
}
