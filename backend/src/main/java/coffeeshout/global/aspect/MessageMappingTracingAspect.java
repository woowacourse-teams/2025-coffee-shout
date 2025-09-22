package coffeeshout.global.aspect;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MessageMappingTracingAspect {

    private final Tracer tracer;

    public MessageMappingTracingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
    public Object traceMessageMapping(ProceedingJoinPoint joinPoint) throws Throwable {

        final String className = joinPoint.getTarget().getClass().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        final String spanName = "websocket.handler." + className + "." + methodName;

        final Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("handler.class", className)
                .setAttribute("handler.method", methodName)
                .setAttribute("message.type", "inbound")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.addEvent("message.handler.start");
            final Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    final String paramType = args[i].getClass().getSimpleName();
                    span.setAttribute("message.param." + i + ".type", paramType);
                }
            }
            final Object result = joinPoint.proceed();
            span.addEvent("message.handler.success");
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.addEvent("message.handler.error");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
