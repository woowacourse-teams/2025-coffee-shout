package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RoomPlayerServiceTest extends ServiceTest {

    @Autowired
    RoomService roomService;

    @Autowired
    RoomPlayerService roomPlayerService;

    @Test
    void 방에_있는_모든_플레이어를_조회한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        SelectedMenuRequest guestSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), guestName, guestSelectedMenuRequest);

        // when
        List<Player> players = roomPlayerService.getAllPlayers(createdRoom.getJoinCode().getValue());

        // then
        assertThat(players).hasSize(2);
        assertThat(players.stream().map(p -> p.getName().value()))
                .containsExactlyInAnyOrder(hostName, guestName);
    }

    @Test
    void 플레이어가_메뉴를_선택한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest initialSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, initialSelectedMenuRequest);

        // when
        List<Player> players = roomPlayerService.selectMenu(createdRoom.getJoinCode().getValue(), hostName, 1L);
        Player host = players.getFirst();

        // then
        assertThat(host.getSelectedMenu().menu().getId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_플레이어가_메뉴를_선택하면_예외가_발생한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest initialSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, initialSelectedMenuRequest);
        String invalidPlayerName = "없는사람";

        // when & then
        assertThatThrownBy(
                () -> roomPlayerService.selectMenu(createdRoom.getJoinCode().getValue(), invalidPlayerName, 3L))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 중복된_이름의_플레이어가_존재하는지_확인한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();

        PlayerName guestName = new PlayerName("게스트1");
        createdRoom.joinGuest(guestName, new SelectedMenu(MenuFixture.아메리카노(), MenuTemperature.ICE));

        // when & then
        assertThat(roomPlayerService.isGuestNameDuplicated(joinCode.getValue(), guestName.value())).isTrue();
        assertThat(roomPlayerService.isGuestNameDuplicated(joinCode.getValue(), "uniqueName")).isFalse();
    }

    @Test
    void 게스트가_준비_상태를_변경한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        SelectedMenuRequest guestSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), guestName, guestSelectedMenuRequest);

        // when
        List<Player> players = roomPlayerService.changePlayerReadyState(
                createdRoom.getJoinCode().getValue(),
                guestName,
                true
        );

        // then
        Player guest = players.stream()
                .filter(p -> p.getName().value().equals(guestName))
                .findFirst()
                .orElseThrow();
        assertThat(guest.getIsReady()).isTrue();
    }

    @Test
    void 호스트가_준비_상태를_변경해도_변경되지_않는다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);

        // when
        List<Player> players = roomPlayerService.changePlayerReadyState(
                createdRoom.getJoinCode().getValue(),
                hostName,
                true
        );

        // then
        Player host = players.stream()
                .filter(p -> p.getName().value().equals(hostName))
                .findFirst()
                .orElseThrow();
        // 호스트는 항상 준비 상태이므로 변경되지 않음
        assertThat(host.getIsReady()).isTrue();
    }

    @Test
    void 플레이어를_제거할_때_플레이어가_없다면_방을_제거한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when
        roomPlayerService.removePlayer(joinCode.getValue(), hostName);

        // then
        assertThat(roomService.roomExists(joinCode.getValue())).isFalse();
    }

    @Test
    void 플레이어를_제거할_때_플레이어가_있다면_방을_제거하지_않는다() {
        String hostName = "호스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();
        roomService.enterRoom(createdRoom.getJoinCode().getValue(), "게스트1",
                new SelectedMenuRequest(2L, null, MenuTemperature.ICE));

        // when
        roomPlayerService.removePlayer(joinCode.getValue(), hostName);

        // then
        assertThat(roomService.roomExists(joinCode.getValue())).isTrue();
    }
}
