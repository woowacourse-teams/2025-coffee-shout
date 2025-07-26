package coffeeshout.global.config;

import coffeeshout.room.application.MenuInitializer;
import coffeeshout.room.domain.service.MenuCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InitConfig {

    private final MenuCommandService menuCommandService;

    @Bean
    public MenuInitializer menuInitializer() {
        return new MenuInitializer(menuCommandService);
    }
}
