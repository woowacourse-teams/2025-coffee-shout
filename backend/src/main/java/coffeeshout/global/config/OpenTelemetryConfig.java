package coffeeshout.global.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.resource.attributes.service.name:coffeeshout-service}")
    private String serviceName;

    @Value("${otel.resource.attributes.service.version:1.0.0}")
    private String serviceVersion;

    @Value("${otel.environment:dev}")
    private String environment;

    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    @Value("${otel.sampling.ratio:1.0}")
    private double samplingRatio;

    @Value("${otel.metrics.export-interval:10}")
    private long metricsExportInterval;

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put(ResourceAttributes.SERVICE_NAME, serviceName)
                        .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                        .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, environment)
                        .build());

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(
                                OtlpGrpcSpanExporter.builder()
                                        .setEndpoint(otlpEndpoint) // OTLP endpoint
                                        .build())
                        .build())
                .setResource(resource)
                .build();

        // 메트릭은 비활성화 (Jaeger는 메트릭 안 받음)
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .build(); // MetricReader 없으면 export 안 함

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
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
