package coffeeshout.global.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpanRepository {

    private static final Map<UUID, Span> spanMap = new ConcurrentHashMap<>();

    public static void add(UUID uuid, Span span) {
        spanMap.put(uuid, span);
    }

    public static void remove(UUID uuid) {
        spanMap.remove(uuid);
    }

    public static Span get(UUID uuid) {
        return spanMap.get(uuid);
    }

    public static void endSpan(UUID uuid, Exception exception) {
        if (!spanMap.containsKey(uuid)) {
            return;
        }
        final Span span = get(uuid);
        if (exception != null) {
            span.recordException(exception);
            span.setStatus(StatusCode.ERROR);
            return;
        }
        span.end();
        remove(uuid);
    }
}
