package coffeeshout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
public class CoffeeShoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeShoutApplication.class, args);
    }

}

