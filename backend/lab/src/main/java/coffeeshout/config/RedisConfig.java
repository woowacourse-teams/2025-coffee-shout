package coffeeshout.config;

import coffeeshout.config.JoinCodeConverter.JoinCodeToStringConverter;
import coffeeshout.config.JoinCodeConverter.StringToJoinCodeConverter;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;

@Configuration
public class RedisConfig {

    @Bean
    @Deprecated
    public RedisCustomConversions redisCustomConversions(
            @Autowired JoinCodeToStringConverter joinCodeToStringConverter,
            @Autowired StringToJoinCodeConverter stringToJoinCodeConverter
    ) {
        return new RedisCustomConversions(Arrays.asList(
                joinCodeToStringConverter,
                stringToJoinCodeConverter
        ));
    }
}
