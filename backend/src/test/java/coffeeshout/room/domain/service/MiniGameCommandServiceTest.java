package coffeeshout.room.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.MiniGameDummy;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class MiniGameCommandServiceTest extends ServiceTest {

    @Autowired
    RoomService roomService;

    @Autowired
    MiniGameCommandService miniGameCommandService;

    @Test
    void 모든_미니게임_목록을_조회한다() {
        // when
        List<MiniGameType> miniGames = miniGameCommandService.getAllMiniGames();

        // then
        assertThat(miniGames).containsExactlyInAnyOrder(MiniGameType.values());
    }

    @Test
    void 미니게임을_선택한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);

        // when
        List<MiniGameType> selectedMiniGames = miniGameCommandService.updateMiniGames(
                createdRoom.getJoinCode().getValue(),
                hostName,
                List.of(MiniGameType.CARD_GAME)
        );

        // then
        assertThat(selectedMiniGames).hasSize(1);
        assertThat(selectedMiniGames.getFirst()).isEqualTo(MiniGameType.CARD_GAME);
    }

    @Test
    void 호스트가_아닌_플레이어가_미니게임을_선택하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        SelectedMenuRequest guestSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), guestName, guestSelectedMenuRequest);

        List<MiniGameType> miniGameTypes = List.of(MiniGameType.CARD_GAME);
        // when & then
        assertThatThrownBy(
                () -> miniGameCommandService.updateMiniGames(
                        createdRoom.getJoinCode().getValue(),
                        guestName,
                        miniGameTypes))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 호스트가_아닌_플레이어가_미니게임을_선택_취소하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        SelectedMenuRequest guestSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), guestName, guestSelectedMenuRequest);
        miniGameCommandService.updateMiniGames(
                createdRoom.getJoinCode().getValue(),
                hostName,
                List.of(MiniGameType.CARD_GAME)
        );

        // when & then
        assertThatThrownBy(() -> miniGameCommandService.updateMiniGames(
                createdRoom.getJoinCode().getValue(),
                guestName,
                List.of(MiniGameType.CARD_GAME)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 미니게임의_점수를_반환한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트1",
                new SelectedMenuRequest(2L, null, MenuTemperature.ICE));
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트2",
                new SelectedMenuRequest(3L, null, MenuTemperature.ICE));

        List<MiniGameDummy> miniGames = List.of(new MiniGameDummy());
        ReflectionTestUtils.setField(createdRoom, "finishedGames", miniGames);

        // when
        Map<Player, MiniGameScore> miniGameScores = miniGameCommandService.getMiniGameScores(
                joinCode.getValue(),
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
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트1",
                new SelectedMenuRequest(2L, null, MenuTemperature.ICE));
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트2",
                new SelectedMenuRequest(3L, null, MenuTemperature.ICE));

        List<MiniGameDummy> miniGames = List.of(new MiniGameDummy());
        ReflectionTestUtils.setField(createdRoom, "finishedGames", miniGames);

        // when
        MiniGameResult miniGameRanks = miniGameCommandService.getMiniGameRanks(
                joinCode.getValue(),
                MiniGameType.CARD_GAME
        );

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
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트1",
                new SelectedMenuRequest(2L, null, MenuTemperature.ICE));
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트2",
                new SelectedMenuRequest(3L, null, MenuTemperature.ICE));
        miniGameCommandService.updateMiniGames(
                createdRoom.getJoinCode().getValue(),
                hostName,
                List.of(MiniGameType.CARD_GAME)
        );

        // when
        List<MiniGameType> selectedMiniGames = miniGameCommandService.getSelectedMiniGames(joinCode.getValue());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectedMiniGames).hasSize(1);
            softly.assertThat(selectedMiniGames).containsExactly(MiniGameType.CARD_GAME);
        });
    }
}
