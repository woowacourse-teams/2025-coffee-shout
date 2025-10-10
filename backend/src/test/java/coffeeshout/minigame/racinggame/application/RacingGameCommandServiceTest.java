package coffeeshout.minigame.racinggame.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.racinggame.application.RacingGameCommandService;
import coffeeshout.racinggame.domain.RacingGame;
import coffeeshout.racinggame.domain.RacingGameState;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.domain.roulette.Probability;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.aspectj.util.Reflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class RacingGameCommandServiceTest extends ServiceTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RacingGameCommandService racingGameCommandService;

    private static final String HOST_NAME = "꾹이";

    private Room room = RoomFixture.호스트_꾹이();
    private RacingGame racingGame = new RacingGame();

    @BeforeEach
    void setUp() {
        room.getPlayers().forEach(player -> player.updateReadyState(true));
        roomRepository.save(room);
    }

    @Test
    void 레이싱_게임을_시작하면_DESCRIPTION_PREPARE_PLAYING_순서로_상태가_전환된다() throws InterruptedException {
        // given
        room.addMiniGame(new PlayerName(HOST_NAME), racingGame);
        room.startNextGame(HOST_NAME);

        // when
        racingGameCommandService.start(room.getJoinCode().getValue(), HOST_NAME);
        Thread.sleep(100);

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.PLAYING);
        assertThat(racingGame.isStarted()).isTrue();
    }
}
