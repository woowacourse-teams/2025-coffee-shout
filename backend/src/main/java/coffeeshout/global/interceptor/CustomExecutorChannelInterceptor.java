package coffeeshout.global.interceptor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

public class CustomExecutorChannelInterceptor implements ExecutorChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CustomExecutorChannelInterceptor.class);
    private final Tracer tracer;
    private final TextMapPropagator propagator;
    private final TextMapGetter<MessageHeaders> headerGetter;

    public CustomExecutorChannelInterceptor(Tracer tracer) {
        this.tracer = tracer;
        this.propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        this.headerGetter = new TextMapGetter<MessageHeaders>() {
            @Override
            public Iterable<String> keys(MessageHeaders carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(MessageHeaders carrier, String key) {
                Object value = carrier.get(key);
                return value != null ? value.toString() : null;
            }
        };
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        // String 헤더들을 추출해서 Context 복원
        Context parentContext = propagator.extract(Context.current(),
                message.getHeaders(),
                headerGetter);

        final Span span = generateSpan(parentContext, message);
        span.addEvent("message.processing.start");

        // Span을 메시지 헤더에 저장하여 후처리에서 사용
        Map<String, Object> mutableHeaders = new HashMap<>(message.getHeaders());
        mutableHeaders.put("otel.span.instance", span);

        return MessageBuilder.fromMessage(message)
                .copyHeaders(mutableHeaders)
                .build();
    }

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
