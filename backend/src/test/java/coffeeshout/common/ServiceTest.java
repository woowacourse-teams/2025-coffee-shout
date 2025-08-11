package coffeeshout.common;

import coffeeshout.config.TestTaskSchedulerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Import(TestTaskSchedulerConfig.class)
@ActiveProfiles("test")
public abstract class ServiceTest {

    @MockitoBean
    protected SimpMessagingTemplate messagingTemplate;

}
