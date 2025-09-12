package coffeeshout.global.config;

import coffeeshout.global.config.properties.RedisProperties;
import coffeeshout.global.config.redis.dto.RoomDto;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameScore;
import coffeeshout.minigame.domain.cardgame.CardHand;
import coffeeshout.minigame.domain.cardgame.PlayerHands;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import coffeeshout.room.domain.player.Winner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = 
            new RedisStandaloneConfiguration(redisProperties.host(), redisProperties.port());
        
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig = 
            LettuceClientConfiguration.builder();
        
        if (redisProperties.ssl().enabled()) {
            clientConfig.useSsl();
        }
        
        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig.build());
    }

    // 기본 Object용 RedisTemplate (호환성 유지용)
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // Room 도메인 객체들 (Redis로 옮길 것들)
    @Bean
    public RedisTemplate<String, Room> roomRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Room> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Room.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Room.class));
        template.afterPropertiesSet();
        return template;
    }

    // RoomDto 전용 RedisTemplate (실제 사용)
    @Bean
    public RedisTemplate<String, RoomDto> roomDtoRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, RoomDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(RoomDto.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(RoomDto.class));
        template.afterPropertiesSet();
        return template;
    }

    // Player 도메인 객체들
    @Bean
    public RedisTemplate<String, Player> playerRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Player> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Player.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Player.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, PlayerName> playerNameRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, PlayerName> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PlayerName.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(PlayerName.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Players> playersRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Players> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Players.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Players.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Winner> winnerRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Winner> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Winner.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Winner.class));
        template.afterPropertiesSet();
        return template;
    }

    // SelectedMenu (Player가 선택한 메뉴 정보)
    @Bean
    public RedisTemplate<String, SelectedMenu> selectedMenuRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, SelectedMenu> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(SelectedMenu.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SelectedMenu.class));
        template.afterPropertiesSet();
        return template;
    }

    // MiniGame 도메인 객체들 (Room과 연관된 것들)
    @Bean
    public RedisTemplate<String, MiniGameResult> miniGameResultRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, MiniGameResult> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(MiniGameResult.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(MiniGameResult.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, MiniGameScore> miniGameScoreRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, MiniGameScore> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(MiniGameScore.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(MiniGameScore.class));
        template.afterPropertiesSet();
        return template;
    }

    // CardGame 도메인 객체들 (Room의 MiniGame으로 포함)
    @Bean
    public RedisTemplate<String, CardGame> cardGameRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, CardGame> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(CardGame.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(CardGame.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, CardGameRound> cardGameRoundRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, CardGameRound> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(CardGameRound.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(CardGameRound.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, CardGameScore> cardGameScoreRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, CardGameScore> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(CardGameScore.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(CardGameScore.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, CardHand> cardHandRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, CardHand> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(CardHand.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(CardHand.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, PlayerHands> playerHandsRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, PlayerHands> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PlayerHands.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(PlayerHands.class));
        template.afterPropertiesSet();
        return template;
    }
}
