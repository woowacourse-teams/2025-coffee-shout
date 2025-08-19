package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.config.TestConfig;
import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.MiniGameDummy;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.TestDataHelper;
import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.roulette.Probability;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class RoomServiceTest {

    @Autowired
    RoomService roomService;

    @Autowired
    TestDataHelper testDataHelper;

    @Test
    void 방을_생성한다() {
        // given
        String hostName = "호스트짱";
        Long menuId = 1L;

        // when
        Room room = roomService.createRoom(hostName, menuId);

        // then
        assertThat(room).isNotNull();
        assertThat(room.getJoinCode()).isNotNull();
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);

        assertThat(room.getPlayers()).hasSize(1);
        assertThat(room.getPlayers().get(0).getName().value()).isEqualTo(hostName);
        assertThat(room.isHost(room.getPlayers().get(0))).isTrue();
    }

    @Test
    void 존재하지_않는_메뉴로_방을_생성하면_예외를_반환한다() {
        // given
        String hostName = "호스트";
        Long invalidMenuId = 999L;

        // when & then
        assertThatThrownBy(() -> roomService.createRoom(hostName, invalidMenuId))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 방에_게스트가_입장한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Long hostMenuId = 1L;
        Long guestMenuId = 2L;

        // 방 먼저 생성
        Room createdRoom = roomService.createRoom(hostName, hostMenuId);
        String joinCode = createdRoom.getJoinCode().value();

        // when
        Room room = roomService.enterRoom(joinCode, guestName, guestMenuId);

        // then
        assertThat(room.getJoinCode().value()).isEqualTo(createdRoom.getJoinCode().value());
        assertThat(room.getPlayers()).hasSize(2);
        assertThat(room.getPlayers().stream().anyMatch(p -> p.getName().value().equals(guestName))).isTrue();
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Test
    void 존재하지_않는_조인코드로_입장하면_예외가_발생한다() {
        // given
        String invalidJoinCode = "ABCDE";
        String guestName = "게스트";
        Long menuId = 1L;

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(invalidJoinCode, guestName, menuId))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 존재하는_방에_입장한다() {
        // given
        String existingJoinCode = "TEST2";
        String guestName = "더미게스트";
        Long menuId = 2L;

        testDataHelper.createDummyRoom(existingJoinCode, "더미호스트");

        // when
        Room room = roomService.enterRoom(existingJoinCode, guestName, menuId);

        // then
        assertThat(room.getJoinCode().value()).isEqualTo(existingJoinCode);
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Test
    void 게임_중인_방에_입장할_수_없다() {
        // given
        String existingJoinCode = "TEST2";
        String guestName = "더미게스트";
        Long menuId = 2L;

        testDataHelper.createDummyPlayingRoom(existingJoinCode, "더미호스트");

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(existingJoinCode, guestName, menuId))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 동일한_조인코드로_여러_게스트가_입장_가능() {
        // given
        String hostName = "호스트짱";
        Long menuId = 1L;
        Room createdRoom = roomService.createRoom(hostName, menuId);
        String joinCode = createdRoom.getJoinCode().value();

        // when
        roomService.enterRoom(joinCode, "게스트1", 2L);
        roomService.enterRoom(joinCode, "게스트2", 3L);
        Room result = roomService.enterRoom(joinCode, "게스트3", 4L);

        // then
        assertThat(result.getPlayers()).hasSize(4);
        assertThat(result.getPlayers().stream().map(p -> p.getName().value()))
                .contains(hostName, "게스트1", "게스트2", "게스트3");
    }

    @Test
    void 최대_인원에서_입장을_하면_예외를_반환한다() {
        // given
        String hostName = "호스트짱";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        String joinCode = createdRoom.getJoinCode().value();

        // 최대 9명까지니까 8명 더 넣어보기
        for (int i = 2; i <= 9; i++) {
            roomService.enterRoom(joinCode, "게스트" + i, 1L);
        }

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(joinCode, "게스트10", 1L))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 중복된_이름으로_입장할_수_없다() {
        // given
        String hostName = "호스트짱";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        String joinCode = createdRoom.getJoinCode().value();
        roomService.enterRoom(joinCode, "게스트", 2L);

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(joinCode, "게스트", 3L))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 잘못된_메뉴_ID로_게스트_입장하면_예외가_발생한다() {
        // given
        String hostName = "호스트짱";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        String joinCode = createdRoom.getJoinCode().value();

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(joinCode, "게스트", 999L))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 방에_있는_모든_플레이어를_조회한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), guestName, 2L);

        // when
        List<Player> players = roomService.getAllPlayers(createdRoom.getJoinCode().value());

        // then
        assertThat(players).hasSize(2);
        assertThat(players.stream().map(p -> p.getName().value()))
                .containsExactlyInAnyOrder(hostName, guestName);
    }

    @Test
    void 플레이어가_메뉴를_선택한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        Long newMenuId = 2L;

        // when
        List<Player> players = roomService.selectMenu(createdRoom.getJoinCode().value(), hostName, newMenuId);
        Player host = players.get(0);

        // then
        assertThat(host.getMenu().getId()).isEqualTo(newMenuId);
    }

    @Test
    void 존재하지_않는_플레이어가_메뉴를_선택하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        String invalidPlayerName = "없는사람";
        Long newMenuId = 3L;

        // when & then
        assertThatThrownBy(
                () -> roomService.selectMenu(createdRoom.getJoinCode().value(), invalidPlayerName,
                        newMenuId)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 플레이어들의_확률을_조회한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), guestName, 2L);

        // when
        Map<Player, Probability> probabilities = roomService.getProbabilities(createdRoom.getJoinCode().value());

        // then
        assertThat(probabilities).hasSize(2);
        assertThat(probabilities.values().stream().mapToDouble(Probability::value).sum()).isEqualTo(10000.0);
    }

    @Test
    void 모든_미니게임_목록을_조회한다() {
        // when
        List<MiniGameType> miniGames = roomService.getAllMiniGames();

        // then
        assertThat(miniGames).containsExactlyInAnyOrder(MiniGameType.values());
    }

    @Test
    void 미니게임을_선택한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);

        // when
        List<MiniGameType> selectedMiniGames = roomService.updateMiniGames(createdRoom.getJoinCode().value(), hostName,
                List.of(MiniGameType.CARD_GAME));

        // then
        assertThat(selectedMiniGames).hasSize(1);
        assertThat(selectedMiniGames.get(0)).isEqualTo(MiniGameType.CARD_GAME);
    }

    @Test
    void 호스트가_아닌_플레이어가_미니게임을_선택하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), guestName, 2L);

        // when & then
        assertThatThrownBy(
                () -> roomService.updateMiniGames(createdRoom.getJoinCode().value(),
                        guestName,
                        List.of(MiniGameType.CARD_GAME)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 호스트가_아닌_플레이어가_미니게임을_선택_취소하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), guestName, 2L);
        roomService.updateMiniGames(createdRoom.getJoinCode().value(), hostName, List.of(MiniGameType.CARD_GAME));

        // when & then
        assertThatThrownBy(() -> roomService.updateMiniGames(createdRoom.getJoinCode().value(), guestName,
                List.of(MiniGameType.CARD_GAME)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 방이_존재하는지_확인한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when & then
        assertThat(roomService.roomExists(joinCode.value())).isTrue();
        assertThat(roomService.roomExists("TRASH")).isFalse();
    }

    @Test
    void 중복된_이름의_플레이어가_존재하는지_확인한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();

        PlayerName guestName = new PlayerName("게스트1");
        createdRoom.joinGuest(guestName, MenuFixture.아메리카노());

        // when & then
        assertThat(roomService.isGuestNameDuplicated(joinCode.value(), guestName.value())).isTrue();
        assertThat(roomService.isGuestNameDuplicated(joinCode.value(), "uniqueName")).isFalse();
    }

    @Test
    void 룰렛을_돌려서_당첨자를_선택한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트1", 2L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트2", 3L);
        ReflectionTestUtils.setField(createdRoom, "roomState", RoomState.PLAYING);

        // when
        Winner winner = roomService.spinRoulette(createdRoom.getJoinCode().value(), hostName);

        // then
        assertThat(winner).isNotNull();
        assertThat(createdRoom.getPlayers().stream().map(Player::getName)).contains(winner.name());
    }

    @Test
    void 미니게임의_점수를_반환한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트1", 2L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트2", 3L);

        List<MiniGameDummy> miniGames = List.of(new MiniGameDummy());
        ReflectionTestUtils.setField(createdRoom, "finishedGames", miniGames);

        // when
        Map<Player, MiniGameScore> miniGameScores = roomService.getMiniGameScores(
                joinCode.value(),
                MiniGameType.CARD_GAME
        );

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(miniGameScores.get(PlayerFixture.호스트꾹이()).getValue()).isEqualTo(20);
            softly.assertThat(miniGameScores.get(PlayerFixture.게스트루키()).getValue()).isEqualTo(-10);
        });
    }

    @Test
    void 미니게임의_순위를_반환한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트1", 2L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트2", 3L);

        List<MiniGameDummy> miniGames = List.of(new MiniGameDummy());
        ReflectionTestUtils.setField(createdRoom, "finishedGames", miniGames);

        // when
        MiniGameResult miniGameRanks = roomService.getMiniGameRanks(joinCode.value(), MiniGameType.CARD_GAME);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(miniGameRanks.getPlayerRank(PlayerFixture.호스트꾹이())).isEqualTo(1);
            softly.assertThat(miniGameRanks.getPlayerRank(PlayerFixture.게스트루키())).isEqualTo(2);
        });
    }

    @Test
    void 선택된_미니게임의_목록을_반환한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트1", 2L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트2", 3L);
        roomService.updateMiniGames(createdRoom.getJoinCode().value(), hostName, List.of(MiniGameType.CARD_GAME));

        // when
        List<MiniGameType> selectedMiniGames = roomService.getSelectedMiniGames(joinCode.value());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectedMiniGames).hasSize(1);
            softly.assertThat(selectedMiniGames).containsExactly(MiniGameType.CARD_GAME);
        });
    }

    @Test
    void 플레이어를_제거할_때_플레이어가_없다면_방을_제거한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when
        roomService.removePlayer(joinCode.value(), hostName);

        // then
        assertThat(roomService.roomExists(joinCode.value())).isFalse();
    }

    @Test
    void 플레이어를_제거할_때_플레이어가_있다면_방을_제거하지_않는다() {
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().value(), "게스트1", 2L);

        // when
        roomService.removePlayer(joinCode.value(), hostName);

        // then
        assertThat(roomService.roomExists(joinCode.value())).isTrue();
    }
}
