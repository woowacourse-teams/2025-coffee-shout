package coffeeshout.global.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class OpenTelemetryConfig {

    private final Environment environment;

    public OpenTelemetryConfig(Environment environment) {
        this.environment = environment;
    }

    @Value("${otel.exporter.otlp.endpoint}")
    private String otlpEndpoint;

    @Bean(destroyMethod = "close")
    public OpenTelemetrySdk openTelemetry() {
        final String[] activeProfiles = environment.getActiveProfiles();
        final String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        final Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put("service.name", "coffeeshout-service")
                        .put("service.version", "1.0.0")
                        .put("deployment.environment", profile)
                        .build());

        final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(
                                OtlpHttpSpanExporter.builder()
                                        .setEndpoint(otlpEndpoint + "/v1/traces")
                                        .build())
                        .build())
                .setResource(resource)
                .build();


        return OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("coffeeshout-service", "1.0.0");
    }

    @Bean
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter("coffeeshout-service 1.0.0");
    }}
