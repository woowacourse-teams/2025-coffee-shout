package coffeeshout.admin.overview.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.AdminModuleServiceTest;
import coffeeshout.admin.overview.domain.DailyTrendPoint;
import coffeeshout.admin.overview.domain.OverviewStatisticsRepository;
import coffeeshout.admin.overview.domain.OverviewStatisticsRepository.GamePlayCount;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.infra.persistence.MiniGameJpaRepository;
import coffeeshout.minigame.infra.persistence.MiniGameResultEntity;
import coffeeshout.minigame.infra.persistence.MiniGameResultJpaRepository;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DATE() 그룹핑과 distinct 집계는 실행해 봐야 안다.
 * 특히 게임별 판 수는 참가자 수만큼 결과 행이 생겨 distinct 를 빼면 부풀어 오른다.
 */
@DisplayName("QueryDslOverviewStatisticsRepository")
class QueryDslOverviewStatisticsRepositoryTest extends AdminModuleServiceTest {

    private static final LocalDateTime WIDE_FROM = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime WIDE_TO = LocalDateTime.of(2999, 1, 1, 0, 0);

    @Autowired
    private OverviewStatisticsRepository overviewStatisticsRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private MiniGameJpaRepository miniGameJpaRepository;

    @Autowired
    private MiniGameResultJpaRepository miniGameResultJpaRepository;

    private RoomEntity room(String joinCode, RoomState status) {
        final RoomEntity room = roomJpaRepository.save(new RoomEntity(joinCode));
        if (status == RoomState.DONE) {
            room.finish();
        } else if (status != RoomState.READY) {
            room.updateRoomStatus(status);
        }
        return roomJpaRepository.save(room);
    }

    @Nested
    class findDailyTrend {

        @Test
        void 같은_날의_방을_하나로_묶는다() {
            room("AAAA", RoomState.READY);
            room("BBBB", RoomState.READY);

            final List<DailyTrendPoint> trend =
                    overviewStatisticsRepository.findDailyTrend(WIDE_FROM, WIDE_TO);

            assertThat(trend).singleElement()
                    .extracting(DailyTrendPoint::created).isEqualTo(2L);
        }

        @Test
        void 상태가_섞여도_생성과_완주를_따로_센다() {
            // 날짜와 상태로 함께 묶은 뒤 자바에서 접는 부분이 실제로 맞는지 본다.
            room("AAAA", RoomState.DONE);
            room("BBBB", RoomState.DONE);
            room("CCCC", RoomState.PLAYING);
            room("DDDD", RoomState.READY);

            final DailyTrendPoint point =
                    overviewStatisticsRepository.findDailyTrend(WIDE_FROM, WIDE_TO).getFirst();

            assertThat(point.created()).isEqualTo(4L);
            assertThat(point.completed()).isEqualTo(2L);
        }

        @Test
        void 참여자_수는_방_수에_영향을_주지_않는다() {
            // 한 쿼리로 조인하면 참여자 수만큼 방이 부풀어 생성 수가 틀린다.
            final RoomEntity room = room("AAAA", RoomState.READY);
            playerJpaRepository.save(new PlayerEntity(room, "철수", PlayerType.HOST));
            playerJpaRepository.save(new PlayerEntity(room, "영희", PlayerType.GUEST));
            playerJpaRepository.save(new PlayerEntity(room, "민수", PlayerType.GUEST));

            final DailyTrendPoint point =
                    overviewStatisticsRepository.findDailyTrend(WIDE_FROM, WIDE_TO).getFirst();

            assertThat(point.created()).isEqualTo(1L);
            assertThat(point.players()).isEqualTo(3L);
        }

        @Test
        void 기간에_아무것도_없으면_빈_목록이다() {
            room("AAAA", RoomState.READY);

            assertThat(overviewStatisticsRepository.findDailyTrend(
                    WIDE_FROM, LocalDateTime.of(2001, 1, 1, 0, 0))).isEmpty();
        }
    }

    @Nested
    class countPlaysByGame {

        @Test
        void 참가자가_여럿이어도_한_판으로_센다() {
            // 한 판에 참가자 수만큼 결과 행이 생긴다. distinct 를 빼면 4명짜리 한 판이 4판이 된다.
            final RoomEntity room = room("AAAA", RoomState.DONE);
            final MiniGameEntity play = miniGameJpaRepository.save(
                    new MiniGameEntity(room.getId(), MiniGameType.RACING_GAME));
            for (int rank = 1; rank <= 4; rank++) {
                final PlayerEntity player = playerJpaRepository.save(
                        new PlayerEntity(room, "p" + rank, PlayerType.GUEST));
                miniGameResultJpaRepository.save(
                        new MiniGameResultEntity(play, player.getId(), rank, 100L));
            }

            assertThat(overviewStatisticsRepository.countPlaysByGame(WIDE_FROM, WIDE_TO))
                    .singleElement()
                    .extracting(GamePlayCount::plays).isEqualTo(1L);
        }

        @Test
        void 게임_타입별로_나눠_센다() {
            final RoomEntity room = room("AAAA", RoomState.DONE);
            final PlayerEntity player =
                    playerJpaRepository.save(new PlayerEntity(room, "철수", PlayerType.HOST));

            final MiniGameEntity racing = miniGameJpaRepository.save(
                    new MiniGameEntity(room.getId(), MiniGameType.RACING_GAME));
            final MiniGameEntity blockStacking = miniGameJpaRepository.save(
                    new MiniGameEntity(room.getId(), MiniGameType.BLOCK_STACKING));
            miniGameResultJpaRepository.save(
                    new MiniGameResultEntity(racing, player.getId(), 1, 100L));
            miniGameResultJpaRepository.save(
                    new MiniGameResultEntity(blockStacking, player.getId(), 1, 200L));

            assertThat(overviewStatisticsRepository.countPlaysByGame(WIDE_FROM, WIDE_TO))
                    .hasSize(2)
                    .allSatisfy(count -> assertThat(count.plays()).isEqualTo(1L));
        }

        @Test
        void 게임_기록이_없으면_빈_목록이다() {
            assertThat(overviewStatisticsRepository.countPlaysByGame(WIDE_FROM, WIDE_TO)).isEmpty();
        }
    }
}
