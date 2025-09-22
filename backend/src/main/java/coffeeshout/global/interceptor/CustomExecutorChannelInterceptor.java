package coffeeshout.global.interceptor;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;

public class CustomExecutorChannelInterceptor implements ExecutorChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CustomExecutorChannelInterceptor.class);
    private final Tracer tracer;
    private final TextMapPropagator propagator;
    private final TextMapGetter<Map<String, String>> headerGetter;

    public CustomExecutorChannelInterceptor(Tracer tracer, TextMapPropagator textMapPropagator) {
        this.tracer = tracer;
        this.propagator = textMapPropagator;
        this.headerGetter = new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
            }
        };
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        Map<String, String> headerMap = new HashMap<>();
        // 2. STOMP nativeHeaders 파싱 (traceparent, tracestate 등 꺼내기)
        Object nativeHeaders = message.getHeaders().get("nativeHeaders");
        if (nativeHeaders instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> nh = (Map<String, List<String>>) nativeHeaders;
            nh.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    headerMap.put(key, values.get(0)); // 첫 번째 값만 사용
                }
            });
        }

        // 3. Context 복원
        Context extractedContext = propagator.extract(Context.current(), headerMap, MAP_GETTER);

        // 4. 부모 span 꺼내기
        Span parentSpan = Span.fromContext(extractedContext);

        // 5. Heartbeat 메시지는 span 종료하지 않고 바로 반환
        if ("HEARTBEAT".equals(headerMap.get("simpMessageType"))) {
            return message;
        }

        // 6. 부모 span 유효하면 종료
        if (parentSpan != null && parentSpan.getSpanContext().isValid()) {
            log.info("end parent span traceId={}, spanId={}",
                    parentSpan.getSpanContext().getTraceId(),
                    parentSpan.getSpanContext().getSpanId());
            parentSpan.end();
        }

        return message;
    }

    private static final TextMapGetter<Map<String, String>> MAP_GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    if (carrier == null) {
                        return null;
                    }
                    return carrier.get(key);
                }
            };

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        Span span = (Span) message.getHeaders().get("otel.span.instance");

        if (span != null && span.getSpanContext().isValid()) {
            try {
                String destination = getDestination(message);
                span.setAttribute("websocket.destination", destination);

                if (ex == null) {
                    span.addEvent("message.sent.success");
                    span.setStatus(StatusCode.OK);
                } else {
                    span.recordException(ex);
                    span.setStatus(StatusCode.ERROR, ex.getMessage());
                    span.addEvent("message.sent.error");
                }
            } finally {
                span.end();
            }
        }
    }

    private Span generateSpan(Context parentContext, Message<?> message) {
        String destination = getDestination(message);
        String spanName = "websocket.message.outbound." + destination.replaceAll("/", ".");

        if (parentContext != null) {
            return tracer.spanBuilder(spanName)
                    .setParent(parentContext)
                    .setSpanKind(SpanKind.PRODUCER)
                    .setAttribute("websocket.destination", destination)
                    .setAttribute("message.type", "outbound")
                    .startSpan();
        } else {
            return tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.PRODUCER)
                    .setAttribute("websocket.destination", destination)
                    .setAttribute("message.type", "outbound")
                    .startSpan();
        }
    }

    private String getDestination(Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        return accessor.getDestination() != null ? accessor.getDestination() : "unknown";
    }
}
