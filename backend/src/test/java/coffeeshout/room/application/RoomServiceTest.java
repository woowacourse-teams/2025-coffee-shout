package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.TestDataHelper;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
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
                .isInstanceOf(NoSuchElementException.class);
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
                .isInstanceOf(NoSuchElementException.class);
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
                .isInstanceOf(IllegalStateException.class);
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
                .isInstanceOf(IllegalStateException.class);
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
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_메뉴_ID로_게스트_입장하면_예외가_발생한다() {
        // given
        String hostName = "호스트짱";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        String joinCode = createdRoom.getJoinCode().value();

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(joinCode, "게스트", 999L))
                .isInstanceOf(NoSuchElementException.class);
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
        List<MiniGameType> selectedMiniGames = roomService.selectMiniGame(createdRoom.getJoinCode().value(), hostName,
                MiniGameType.CARD_GAME);

        // then
        assertThat(selectedMiniGames).hasSize(1);
        assertThat(selectedMiniGames.get(0)).isEqualTo(MiniGameType.CARD_GAME);
    }

    @Test
    void 미니게임을_선택_취소한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.selectMiniGame(createdRoom.getJoinCode().value(), hostName, MiniGameType.CARD_GAME);

        // when
        List<MiniGameType> selectedMiniGames = roomService.unselectMiniGame(
                createdRoom.getJoinCode().value(),
                hostName,
                MiniGameType.CARD_GAME
        );

        // then
        assertThat(selectedMiniGames).isEmpty();
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
                () -> roomService.selectMiniGame(createdRoom.getJoinCode().value(), guestName, MiniGameType.CARD_GAME))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 호스트가_아닌_플레이어가_미니게임을_선택_취소하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        roomService.enterRoom(createdRoom.getJoinCode().value(), guestName, 2L);
        roomService.selectMiniGame(createdRoom.getJoinCode().value(), hostName, MiniGameType.CARD_GAME);

        // when & then
        assertThatThrownBy(() -> roomService.unselectMiniGame(createdRoom.getJoinCode().value(), guestName,
                MiniGameType.CARD_GAME))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 방이_존재하는지_확인한다() {
        // given
        String hostName = "호스트";
        Room createdRoom = roomService.createRoom(hostName, 1L);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when & then
        assertThat(roomService.isRoomExists(joinCode.value())).isTrue();
        assertThat(roomService.isRoomExists("TRASH")).isFalse();
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
        Player losePlayer = roomService.spinRoulette(createdRoom.getJoinCode().value(), hostName);

        // then
        assertThat(losePlayer).isNotNull();
        assertThat(createdRoom.getPlayers()).contains(losePlayer);
    }
}
