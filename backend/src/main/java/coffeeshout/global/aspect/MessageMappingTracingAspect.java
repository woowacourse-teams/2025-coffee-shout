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

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Span span = tracer.spanBuilder("websocket.message." + methodName)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("handler.class", className)
                .setAttribute("handler.method", methodName)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    span.setAttribute("message.param." + i, args[i].toString());
                }
            }

            Object result = joinPoint.proceed();
            span.setStatus(StatusCode.OK);
            return result;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
