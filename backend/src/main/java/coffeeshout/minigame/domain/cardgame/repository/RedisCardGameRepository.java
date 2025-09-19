package coffeeshout.minigame.domain.cardgame.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisCardGameRepository implements CardGameRepository {

    private static final String CARD_GAME_KEY = "cardGame:%s";
    private static final Duration CARD_GAME_DEFAULT_TTL = Duration.ofMinutes(10);
    private static final String FIELD_INFO = "info";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<CardGame> findByJoinCode(JoinCode joinCode) {
        final Object cardGame = redisTemplate.opsForHash().get(
                String.format(CARD_GAME_KEY, joinCode.getValue()),
                FIELD_INFO
        );

        if (cardGame == null) {
            return Optional.empty();
        }
        if (!(cardGame instanceof CardGame)) {
            throw new IllegalStateException("저장된 객체의 타입이 CardGame이 아닙니다.");
        }

        return Optional.of((CardGame) cardGame);
    }

    @Override
    public CardGame save(CardGame cardGame) {
        final String key = createRedisKey(cardGame.getJoinCode());
        redisTemplate.opsForHash().put(
                key,
                FIELD_INFO,
                cardGame
        );
        redisTemplate.expire(key, CARD_GAME_DEFAULT_TTL);
        return cardGame;
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        redisTemplate.opsForHash().delete(String.format(CARD_GAME_KEY, joinCode.getValue()), FIELD_INFO);
    }

    private String createRedisKey(JoinCode joinCode) {
        return String.format(CARD_GAME_KEY, joinCode.getValue());
    }
}
