package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import coffeeshout.global.ServiceTest;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.QrCodeStatus;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import coffeeshout.fixture.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class RoomServiceTest extends ServiceTest {

    @Autowired
    RoomService roomService;

    @Autowired
    TestDataHelper testDataHelper;

    @MockitoSpyBean
    DelayedRoomRemovalService delayedRoomRemovalService;

    @Autowired
    JoinCodeGenerator joinCodeGenerator;

    @Autowired
    RoomQueryService roomQueryService;

    @Test
    void 방을_생성한다() {
        // given
        String hostName = "호스트짱";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);

        // when
        Room room = roomService.createRoom(hostName, selectedMenuRequest);

        // then
        assertThat(room).isNotNull();
        assertThat(room.getJoinCode()).isNotNull();
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);

        assertThat(room.getPlayers()).hasSize(1);
        assertThat(room.getPlayers().getFirst().getName().value()).isEqualTo(hostName);
        assertThat(room.isHost(room.getPlayers().getFirst())).isTrue();
    }

    @Test
    void 존재하지_않는_메뉴로_방을_생성하면_예외를_반환한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(999L, null, MenuTemperature.ICE);

        // when & then
        assertThatThrownBy(() -> roomService.createRoom(hostName, selectedMenuRequest))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 방에_게스트가_입장한다() {
        // given
        String hostName = "호스트";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        SelectedMenuRequest guestSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);

        // 방 먼저 생성
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();

        // when
        Room room = roomService.enterRoom(joinCode, guestName, guestSelectedMenuRequest);

        // then
        assertThat(room.getJoinCode().getValue()).isEqualTo(createdRoom.getJoinCode().getValue());
        assertThat(room.getPlayers()).hasSize(2);
        assertThat(room.getPlayers().stream().anyMatch(p -> p.getName().value().equals(guestName))).isTrue();
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Test
    void 존재하지_않는_조인코드로_입장하면_예외가_발생한다() {
        // given
        String invalidJoinCode = "ABCD";
        String guestName = "게스트";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);

        // when & then
        assertThatThrownBy(() -> roomService.enterRoom(invalidJoinCode, guestName, hostSelectedMenuRequest))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 존재하는_방에_입장한다() {
        // given
        JoinCode existingJoinCode = joinCodeGenerator.generate();
        PlayerName guestName = new PlayerName("더미게스트");
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);

        testDataHelper.createDummyRoom(existingJoinCode, new PlayerName("더미호스트"));

        // when
        Room room = roomService.enterRoom(existingJoinCode.getValue(), guestName.value(), hostSelectedMenuRequest);

        // then
        assertThat(room.getJoinCode().getValue()).isEqualTo(existingJoinCode.getValue());
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Test
    void 게임_중인_방에_입장할_수_없다() {
        // given
        JoinCode existingJoinCode = joinCodeGenerator.generate();
        PlayerName guestName = new PlayerName("더미게스트");
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(2L, null, MenuTemperature.ICE);

        testDataHelper.createDummyPlayingRoom(existingJoinCode, new PlayerName("더미호스트"));

        // when & then
        assertThatThrownBy(
                () -> roomService.enterRoom(existingJoinCode.getValue(), guestName.value(), hostSelectedMenuRequest))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void 동일한_조인코드로_여러_게스트가_입장_가능() {
        // given
        String hostName = "호스트짱";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();

        // when
        roomService.enterRoom(joinCode, "게스트1", new SelectedMenuRequest(2L, null, MenuTemperature.ICE));
        roomService.enterRoom(joinCode, "게스트2", new SelectedMenuRequest(3L, null, MenuTemperature.ICE));
        Room result = roomService.enterRoom(joinCode, "게스트3", new SelectedMenuRequest(4L, null, MenuTemperature.ICE));

        // then
        assertThat(result.getPlayers()).hasSize(4);
        assertThat(result.getPlayers().stream().map(p -> p.getName().value()))
                .contains(hostName, "게스트1", "게스트2", "게스트3");
    }

    @Test
    void 최대_인원에서_입장을_하면_예외를_반환한다() {
        // given
        String hostName = "호스트짱";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();

        // 최대 9명까지니까 8명 더 넣어보기
        for (int i = 2; i <= 9; i++) {
            roomService.enterRoom(joinCode, "게스트" + i, new SelectedMenuRequest(1L, null, MenuTemperature.ICE));
        }

        // when & then
        assertThatThrownBy(
                () -> roomService.enterRoom(joinCode, "게스트10", new SelectedMenuRequest(1L, null, MenuTemperature.ICE)))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void 중복된_이름으로_입장할_수_없다() {
        // given
        String hostName = "호스트짱";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();
        roomService.enterRoom(joinCode, "게스트", new SelectedMenuRequest(2L, null, MenuTemperature.ICE));

        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(3L, null, MenuTemperature.ICE);

        // when & then
        assertThatThrownBy(
                () -> roomService.enterRoom(joinCode, "게스트", selectedMenuRequest))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void 잘못된_메뉴_ID로_게스트_입장하면_예외가_발생한다() {
        // given
        String hostName = "호스트짱";
        SelectedMenuRequest hostSelectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, hostSelectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();

        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(999L, null, MenuTemperature.ICE);

        // when & then
        assertThatThrownBy(
                () -> roomService.enterRoom(joinCode, "게스트", selectedMenuRequest))
                .isInstanceOf(NotExistElementException.class);
    }

    @Test
    void 방이_존재하는지_확인한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when & then
        assertThat(roomService.roomExists(joinCode.getValue())).isTrue();
        assertThat(roomService.roomExists("TRAS")).isFalse();
    }

    @Test
    void 플레이어를_제거할_때_플레이어가_없다면_방을_제거한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();

        // when
        roomService.removePlayer(joinCode.getValue(), hostName);

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
        roomService.removePlayer(joinCode.getValue(), hostName);

        // then
        assertThat(roomService.roomExists(joinCode.getValue())).isTrue();
    }

    @Test
    void 방_생성_시_QR_코드가_비동기로_생성된다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);

        // when
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        JoinCode joinCode = createdRoom.getJoinCode();

        // then

        // 비동기 작업이 완료될 때까지 대기 (최대 3초)
        await().atMost(3, SECONDS)
                .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Room room = roomQueryService.getByJoinCode(joinCode);
                    QrCodeStatus status = room.getJoinCode().getQrCode().getStatus();

                    // SUCCESS 또는 ERROR 상태로 변경되었는지 확인
                    assertThat(status).isIn(QrCodeStatus.SUCCESS, QrCodeStatus.ERROR);
                });
    }

    @Test
    void QR코드_상태를_조회한다() {
        // given
        String hostName = "호스트";
        SelectedMenuRequest selectedMenuRequest = new SelectedMenuRequest(1L, null, MenuTemperature.ICE);
        Room createdRoom = roomService.createRoom(hostName, selectedMenuRequest);
        String joinCode = createdRoom.getJoinCode().getValue();

        // when
        QrCodeStatusResponse qrCodeStatus = roomService.getQrCodeStatus(joinCode);

        // then
        assertThat(qrCodeStatus.status()).isIn(QrCodeStatus.PENDING, QrCodeStatus.SUCCESS, QrCodeStatus.ERROR);
    }

    @Test
    void 존재하지_않는_방의_QR코드_상태를_조회하면_예외를_반환한다() {
        // given
        String nonExistentJoinCode = "NXNX";

        // when & then
        assertThatThrownBy(() -> roomService.getQrCodeStatus(nonExistentJoinCode))
                .isInstanceOf(NotExistElementException.class);
    }
}
