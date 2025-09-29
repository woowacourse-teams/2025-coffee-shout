package coffeeshout.global.event;

import coffeeshout.global.config.trace.ObservationRegistryProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis publish를 위한 이벤트들의 기본 클래스 tracing 정보(traceId, spanId)를 포함하여 분산 추적 지원
 */
public abstract class BaseEvent {
    protected final String eventId;
    protected final String traceId;
    protected final String spanId;
    protected final LocalDateTime timestamp;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        final ObservationRegistry observationRegistry = ObservationRegistryProvider.getObservationRegistry();
        final Observation observation = observationRegistry.getCurrentObservation();
        if (observation == null) {
            this.traceId = "";
            this.spanId = "";
            return;
        }
        final TracingContext traceContext = observation.getContext().get(TracingContext.class);
        if (traceContext == null || traceContext.getSpan() == null) {
            this.traceId = "";
            this.spanId = "";
            return;
        }
        this.traceId = traceContext.getSpan().context().traceId();
        this.spanId = traceContext.getSpan().context().spanId();
    }

    public String getEventId() {
        return eventId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
