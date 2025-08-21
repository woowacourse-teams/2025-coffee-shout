package coffeeshout.common;

import coffeeshout.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public abstract class ServiceTest {

    @MockitoBean
    protected SimpMessagingTemplate messagingTemplate;

}
