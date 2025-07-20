package coffeeshout.room.domain;

import static coffeeshout.fixture.PlayerFixture.꾹이;
import static coffeeshout.fixture.PlayerFixture.루키;
import static coffeeshout.fixture.PlayerFixture.엠제이;
import static coffeeshout.fixture.PlayerFixture.한스;
import static org.assertj.core.api.Assertions.*;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.probability.PlayersWithProbability;
import coffeeshout.room.domain.probability.Probability;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class RouletteRoomTest {

    Player host = 한스();
    JoinCode joinCode = JoinCode.generate();
    RandomGenerator randomGenerator = new FixedLastValueGenerator();
    RouletteRoom room = new RouletteRoom(joinCode, host, randomGenerator);

    @Test
    void 방의_초기_상태는_READY이다() {
        // then
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Nested
    class 방에_참여하는_로직_검증 {

        @Test
        void 이름이_같은_플레이어가_존재하면_참여할_수_없다() {
            // given

            // when & then
            assertThatThrownBy(() -> room.joinGuest(new Player(host.getName())))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(value = RoomState.class, names = "READY", mode = EnumSource.Mode.EXCLUDE)
        void 준비중인_방에만_참여할_수_있다(RoomState state) {
            // given
            ReflectionTestUtils.setField(room, "roomState", state);

            // when & then
            assertThatThrownBy(() -> room.joinGuest(한스()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 방에_정상적으로_참여한다() {
            // given

            // when
            room.joinGuest(꾹이());

            // then
            assertThat(room.getPlayers()).hasSize(2);
        }
    }

    @Nested
    class 방에_미니게임을_설정한다 {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        void 미니게임은_한개_이상_다섯개_이하로_설정해야_한다(int miniGameSize) {
            // given
            List<Playable> miniGames = IntStream.range(0, miniGameSize)
                    .mapToObj(i -> new DummyMiniGame())
                    .collect(Collectors.toUnmodifiableList());

            room.setMiniGame(miniGames);

            // when & then
            assertThat(room.getMiniGames()).hasSize(miniGameSize);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 6})
        void 미니게임을_한개_미만_다섯개_초과로_설정하면_예외_발생(int miniGameSize) {
            // given
            List<Playable> miniGames = IntStream.range(0, miniGameSize)
                    .mapToObj(i -> new DummyMiniGame())
                    .collect(Collectors.toUnmodifiableList());

            // when & then
            assertThatThrownBy(() -> room.setMiniGame(miniGames))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class 플레이어별_확률계산 {

        @Test
        void 미니게임의_결과가_없을_경우_즉_초기_확률을_계산한다() {
            // given
            room.joinGuest(꾹이());
            room.joinGuest(루키());
            room.joinGuest(엠제이());

            // when
            PlayersWithProbability playersWithProbability = room.getProbabilities();

            // then
            Probability expected = new Probability(2500);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(playersWithProbability.getProbability(꾹이())).isEqualTo(expected);
                softly.assertThat(playersWithProbability.getProbability(한스())).isEqualTo(expected);
                softly.assertThat(playersWithProbability.getProbability(엠제이())).isEqualTo(expected);
                softly.assertThat(playersWithProbability.getProbability(루키())).isEqualTo(expected);
            });
        }

        @Test
        void 미니게임을_진행한_후_확률을_계산한다() {
            // given
            room.joinGuest(꾹이());
            room.joinGuest(루키());
            room.joinGuest(엠제이());

            /*
                - 각 플레이어 초기확률 2500
                - 총 2라운드 → step = 625
             */
            room.setMiniGame(List.of(new DummyMiniGame(), new DummyMiniGame()));
            room.addMiniGameResult(new MiniGameResult(Map.of(
                    한스(), 1, // 2500 + 1250
                    꾹이(), 2, // 2500 + 625
                    엠제이(), 3, // 2500 - 625
                    루키(), 4 // 2500 - 1250
            )));

            // when
            PlayersWithProbability playersWithProbability = room.getProbabilities();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(playersWithProbability.getProbability(한스())).isEqualTo(new Probability(3750));
                softly.assertThat(playersWithProbability.getProbability(꾹이())).isEqualTo(new Probability(3125));
                softly.assertThat(playersWithProbability.getProbability(엠제이())).isEqualTo(new Probability(1875));
                softly.assertThat(playersWithProbability.getProbability(루키())).isEqualTo(new Probability(1250));
            });
        }
    }

    @Nested
    class 룰렛을_돌린다 {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
        void 방의_참여_인원이_2에서_9명이면_룰렛을_돌릴_수_있다(int playerCount) {
            // given
            IntStream.range(1, playerCount + 1).forEach(index -> {
                room.joinGuest(new Player(String.format("player%d", index)));
            });
            ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

            // when & then
            Player spin = room.spin(); // FixedLastValueGenerator = 항상 마지막에 추가된 플레이어가 당첨
            assertThat(spin).isEqualTo(new Player(String.format("player%d", playerCount)));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 9})
        void 방의_참여_인원이_2에서_9명이_아닐_경우_룰렛을_돌리면_예외_발생(int playerCount) {
            // given
            IntStream.range(1, playerCount + 1).forEach(index -> {
                room.joinGuest(new Player(String.format("player%d", index)));
            });
            ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

            // when & then
            assertThatThrownBy(() -> room.spin()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void 호스트인지_확인한다() {
        // given
        room.joinGuest(꾹이());

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(room.isHost(한스())).isTrue();
            softly.assertThat(room.isHost(꾹이())).isFalse();
        });
    }
}
