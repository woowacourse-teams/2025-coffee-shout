package coffeeshout.room.application;

import coffeeshout.global.ServiceTest;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.room.infra.persistence.RouletteResultJpaRepository;
import coffeeshout.room.ui.response.TopWinnerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest extends ServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private RouletteResultJpaRepository rouletteResultJpaRepository;

    @Test
    @DisplayName("이번달 가장 많이 당첨된 닉네임 상위 5개를 조회한다")
    void getTop5Winners() {
        // given
        final RoomEntity room = roomJpaRepository.save(new RoomEntity("ABCD"));

        final PlayerEntity player1 = playerJpaRepository.save(
                new PlayerEntity(room, "철수", PlayerType.HOST)
        );
        final PlayerEntity player2 = playerJpaRepository.save(
                new PlayerEntity(room, "영희", PlayerType.GUEST)
        );
        final PlayerEntity player3 = playerJpaRepository.save(
                new PlayerEntity(room, "민수", PlayerType.GUEST)
        );

        // 철수 5번 당첨
        for (int i = 0; i < 5; i++) {
            rouletteResultJpaRepository.save(new RouletteResultEntity(room, player1, 50));
        }

        // 영희 3번 당첨
        for (int i = 0; i < 3; i++) {
            rouletteResultJpaRepository.save(new RouletteResultEntity(room, player2, 30));
        }

        // 민수 2번 당첨
        for (int i = 0; i < 2; i++) {
            rouletteResultJpaRepository.save(new RouletteResultEntity(room, player3, 20));
        }

        // when
        final List<TopWinnerResponse> result = dashboardService.getTop5Winners();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).nickname()).isEqualTo("철수");
        assertThat(result.get(0).winCount()).isEqualTo(5);
        assertThat(result.get(1).nickname()).isEqualTo("영희");
        assertThat(result.get(1).winCount()).isEqualTo(3);
        assertThat(result.get(2).nickname()).isEqualTo("민수");
        assertThat(result.get(2).winCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("이번달 당첨 기록이 없으면 빈 리스트를 반환한다")
    void getTop5Winners_emptyList() {
        // when
        final List<TopWinnerResponse> result = dashboardService.getTop5Winners();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 닉네임의 당첨은 합산된다")
    void getTop5Winners_sameNickname() {
        // given
        final RoomEntity room1 = roomJpaRepository.save(new RoomEntity("AAAA"));
        final RoomEntity room2 = roomJpaRepository.save(new RoomEntity("BBBB"));

        final PlayerEntity player1 = playerJpaRepository.save(
                new PlayerEntity(room1, "철수", PlayerType.HOST)
        );
        final PlayerEntity player2 = playerJpaRepository.save(
                new PlayerEntity(room2, "철수", PlayerType.HOST)
        );

        rouletteResultJpaRepository.save(new RouletteResultEntity(room1, player1, 50));
        rouletteResultJpaRepository.save(new RouletteResultEntity(room2, player2, 30));

        // when
        final List<TopWinnerResponse> result = dashboardService.getTop5Winners();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nickname()).isEqualTo("철수");
        assertThat(result.get(0).winCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("5개 이상이면 상위 5개만 반환한다")
    void getTop5Winners_limitFive() {
        // given
        final RoomEntity room = roomJpaRepository.save(new RoomEntity("CCCC"));

        for (int i = 1; i <= 10; i++) {
            final PlayerEntity player = playerJpaRepository.save(
                    new PlayerEntity(room, "플레이어" + i, PlayerType.GUEST)
            );

            for (int j = 0; j < i; j++) {
                rouletteResultJpaRepository.save(new RouletteResultEntity(room, player, 10));
            }
        }

        // when
        final List<TopWinnerResponse> result = dashboardService.getTop5Winners();

        // then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).nickname()).isEqualTo("플레이어10");
        assertThat(result.get(0).winCount()).isEqualTo(10);
        assertThat(result.get(4).nickname()).isEqualTo("플레이어6");
        assertThat(result.get(4).winCount()).isEqualTo(6);
    }
}
