package coffeeshout.global.config.aspect;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageMappingTracingAspect {

    private final ObservationRegistry observationRegistry;

    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
    public Object traceMessageMapping(ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();

        final String spanName = "websocket:" + method.getName();

        return Observation.createNotStarted(spanName, observationRegistry)
                .lowCardinalityKeyValue("method.name", method.getName())
                .lowCardinalityKeyValue("class.name", method.getDeclaringClass().getSimpleName())
                .observeChecked(() -> joinPoint.proceed());
    }
}
