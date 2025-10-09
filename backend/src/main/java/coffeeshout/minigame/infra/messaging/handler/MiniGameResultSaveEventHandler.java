package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.cardgame.domain.CardGame;
import coffeeshout.cardgame.domain.CardGameTaskType;
import coffeeshout.minigame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.infra.persistence.MiniGameJpaRepository;
import coffeeshout.minigame.infra.persistence.MiniGameResultEntity;
import coffeeshout.minigame.infra.persistence.MiniGameResultJpaRepository;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiniGameResultSaveEventHandler {

    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final MiniGameJpaRepository miniGameJpaRepository;
    private final MiniGameResultJpaRepository miniGameResultJpaRepository;

    @EventListener
    @Transactional
    public void handle(CardGameStateChangedEvent event) {
        if (event.currentTask() != CardGameTaskType.GAME_FINISH_STATE) {
            return;
        }

        tryDbSaveResult(event);
    }

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "minigame:result:lock:",
            donePrefix = "minigame:result:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    private void tryDbSaveResult(CardGameStateChangedEvent event) {
        final String joinCode = event.room().getJoinCode().getValue();

        final RoomEntity roomEntity = roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다: " + joinCode));

        final MiniGameEntity miniGameEntity = miniGameJpaRepository
                .findByRoomSessionAndMiniGameType(roomEntity, MiniGameType.CARD_GAME)
                .orElseThrow(() -> new IllegalArgumentException("카드게임 엔티티가 존재하지 않습니다: " + joinCode));

        final CardGame cardGame = event.cardGame();
        final MiniGameResult result = cardGame.getResult();
        final Map<Player, MiniGameScore> scores = cardGame.getScores();

        for (Player player : event.room().getPlayers()) {
            final PlayerEntity playerEntity = playerJpaRepository.findByRoomSessionAndPlayerName(roomEntity,
                            player.getName().value())
                    .orElseThrow(() -> new IllegalArgumentException("플레이어가 존재하지 않습니다: " + player.getName().value()));

            final Integer rank = result.getPlayerRank(player);
            final Integer score = Math.toIntExact(scores.get(player).getValue());

            final MiniGameResultEntity resultEntity = new MiniGameResultEntity(
                    miniGameEntity,
                    playerEntity,
                    rank,
                    score
            );

            miniGameResultJpaRepository.save(resultEntity);
        }

        log.info("미니게임 결과 저장 완료: joinCode={}", joinCode);
    }
}
