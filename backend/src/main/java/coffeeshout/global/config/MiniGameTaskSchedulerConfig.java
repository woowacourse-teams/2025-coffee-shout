package coffeeshout.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@Slf4j
public class MiniGameTaskSchedulerConfig {

    @Bean(name = "miniGameTaskScheduler")
    public TaskScheduler miniGameTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 스레드 풀 크기 (동시 실행되는 스케줄 작업 수에 따라 조정)
        scheduler.setPoolSize(3);

        // 스레드 이름 접두사
        scheduler.setThreadNamePrefix("mini-game-task");

        // 스레드가 데몬 스레드로 동작할지 여부
        scheduler.setDaemon(false);

        // 에러 핸들러 (스케줄 실행 중 예외 로깅)
        scheduler.setErrorHandler(t ->
                log.error("스케줄 실행 중 예외가 발생했습니다.")
        );

        // 종료 시 현재 실행 중인 작업 완료 대기
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        scheduler.initialize();
        return scheduler;
    }
}
