package coffeeshout.global.config.trace;

import io.micrometer.tracing.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TraceContextProvider {

    public static TraceContext generateTraceContext(String traceId, String spanId) {
        return new TraceContext() {
            @Override
            public String traceId() {
                return traceId;
            }

            @Override
            public String spanId() {
                return spanId;
            }

            @Override
            public String parentId() {
                return null;
            }

            @Override
            public Boolean sampled() {
                return true;
            }
        };
    }
}
