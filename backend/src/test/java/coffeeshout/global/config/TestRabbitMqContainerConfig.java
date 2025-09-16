package coffeeshout.global.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class TestRabbitMqContainerConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestRabbitMqContainerConfig.class);
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";

    private static GenericContainer<?> rabbitmqContainer;

    static {
        initializeContainer();
    }

    private static void initializeContainer() {
        if (rabbitmqContainer == null) {
            logger.info("Initializing RabbitMQ TestContainer...");

            rabbitmqContainer = new GenericContainer<>("pcloud/rabbitmq-stomp")
                    .withExposedPorts(5672, 61613)
                    .withEnv("RABBITMQ_DEFAULT_USER", DEFAULT_USERNAME)
                    .withEnv("RABBITMQ_DEFAULT_PASS", DEFAULT_PASSWORD)
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofMinutes(1))
                    .withReuse(true)
                    .withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("RABBITMQ"));

            rabbitmqContainer.start();

            logger.info("RabbitMQ container started on {}:{} (AMQP), {}:{} (STOMP)",
                    rabbitmqContainer.getHost(), rabbitmqContainer.getMappedPort(5672),
                    rabbitmqContainer.getHost(), rabbitmqContainer.getMappedPort(61613));
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (rabbitmqContainer != null && rabbitmqContainer.isRunning()) {
            String host = rabbitmqContainer.getHost();
            Integer amqpPort = rabbitmqContainer.getMappedPort(5672);
            Integer stompPort = rabbitmqContainer.getMappedPort(61613);

            // RabbitMQ connection properties
            registry.add("spring.rabbitmq.host", () -> host);
            registry.add("spring.rabbitmq.port", () -> amqpPort);
            registry.add("spring.rabbitmq.username", () -> DEFAULT_USERNAME);
            registry.add("spring.rabbitmq.password", () -> DEFAULT_PASSWORD);

            // WebSocket STOMP relay properties
            registry.add("spring.websocket.stomp.relay.host", () -> host);
            registry.add("spring.websocket.stomp.relay.port", () -> stompPort);
            registry.add("spring.websocket.stomp.relay.login", () -> DEFAULT_USERNAME);
            registry.add("spring.websocket.stomp.relay.passcode", () -> DEFAULT_PASSWORD);
        }
    }
}
