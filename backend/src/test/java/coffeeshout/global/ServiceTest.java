package coffeeshout.global;

import coffeeshout.global.config.ServiceTestConfig;
import coffeeshout.global.config.TestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import({ServiceTestConfig.class, TestContainerConfig.class})
@ActiveProfiles("test")
public abstract class ServiceTest {

    @MockitoBean
    protected ApplicationEventPublisher eventPublisher;

}
