package coffeeshout.global;

import coffeeshout.global.config.ServiceTestConfig;
import coffeeshout.global.config.TestRabbitMqContainerConfig;
import coffeeshout.global.config.TestRedisContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import({ServiceTestConfig.class, TestRedisContainerConfig.class, TestRabbitMqContainerConfig.class})
@ActiveProfiles("test")
public abstract class ServiceTest {

    @MockitoBean
    protected SimpMessagingTemplate messagingTemplate;

}
