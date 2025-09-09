package coffeeshout;

import coffeeshout.global.config.properties.QrProperties;
import coffeeshout.global.config.properties.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({QrProperties.class, S3Properties.class})
@SpringBootApplication
public class CoffeeShoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeShoutApplication.class, args);
    }

}

