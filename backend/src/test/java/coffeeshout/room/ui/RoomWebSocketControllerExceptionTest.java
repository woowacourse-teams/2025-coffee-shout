package coffeeshout.room.ui;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.TypeReferenceFrameHandler;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.response.PlayerResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RoomWebSocketControllerExceptionTest extends WebSocketIntegrationTestSupport {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MenuRepository menuRepository;

    private Menu testMenu;

    @BeforeEach
    void setUp() {
        // given
        testMenu = menuRepository.findAll().get(0);
    }

    @Test
    void 존재하지_않는_방에_접근할_때_예외가_발생한다() throws Exception {
        // given
        String nonExistentJoinCode = "XXXXX";
        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
        TypeReferenceFrameHandler<WebSocketResponse<List<PlayerResponse>>> handler = getHandler(responseQueue,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                });

        // when
        session.subscribe("/topic/room/" + nonExistentJoinCode, handler);
        session.send("/app/room/" + nonExistentJoinCode + "/players", null);

        // then
        WebSocketResponse<List<PlayerResponse>> response = responseQueue.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();

        assertThat(response.errorMessage()).isEqualTo("처리 중 오류가 발생했습니다.");
    }

    @Test
    void 존재하지_않는_메뉴로_변경을_시도할_때_예외가_발생한다() throws Exception {
        // given
        JoinCode joinCode = new JoinCode("TEST2");
        PlayerName hostName = new PlayerName("호스트꾹이");
        Room testRoom = Room.createNewRoom(joinCode, hostName,
                testMenu);
        roomRepository.save(testRoom);

        String invalidMenuId = "999999";
        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
        TypeReferenceFrameHandler<WebSocketResponse<List<PlayerResponse>>> handler = getHandler(responseQueue,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                });

        // when
        session.subscribe("/topic/room/" + joinCode.value(), handler);
        MenuChangeMessage message = new MenuChangeMessage(hostName.value(), Long.parseLong(invalidMenuId));
        session.send("/app/room/" + joinCode.value() + "/menus", message);

        // then
        WebSocketResponse<List<PlayerResponse>> response = responseQueue.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();

        assertThat(response.errorMessage()).isEqualTo("처리 중 오류가 발생했습니다.");
    }

    @Test
    void 존재하지_않는_플레이어로_메뉴_변경을_시도할_때_예외가_발생한다() throws Exception {
        // given
        JoinCode joinCode = new JoinCode("TEST3");
        PlayerName hostName = new PlayerName("호스트꾹이");
        Room testRoom = Room.createNewRoom(joinCode, hostName,
                testMenu);
        roomRepository.save(testRoom);

        String nonExistentPlayerName = "존재하지않는플레이어";
        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
        TypeReferenceFrameHandler<WebSocketResponse<List<PlayerResponse>>> handler = getHandler(responseQueue,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                });

        // when
        session.subscribe("/topic/room/" + joinCode.value(), handler);
        MenuChangeMessage message = new MenuChangeMessage(nonExistentPlayerName, testMenu.getId());
        session.send("/app/room/" + joinCode.value() + "/menus", message);

        // then
        WebSocketResponse<List<PlayerResponse>> response = responseQueue.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.errorMessage()).isEqualTo("처리 중 오류가 발생했습니다.");
    }

    @Test
    void 잘못된_형식의_메시지를_전송할_때_예외가_발생한다() throws Exception {
        // given
        JoinCode joinCode = new JoinCode("TEST4");
        PlayerName hostName = new PlayerName("호스트꾹이");
        Room testRoom = Room.createNewRoom(joinCode, hostName, testMenu);
        roomRepository.save(testRoom);

        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
        TypeReferenceFrameHandler<WebSocketResponse<List<PlayerResponse>>> handler = getHandler(responseQueue,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                });

        // when
        session.subscribe("/topic/room/" + joinCode.value(), handler);
        // 잘못된 형식의 메시지 전송 (null 대신 빈 문자열)
        session.send("/app/room/" + joinCode.value() + "/menus", "");

        // then
        WebSocketResponse<List<PlayerResponse>> response = responseQueue.poll(5, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.errorMessage()).isEqualTo("처리 중 오류가 발생했습니다.");
    }

    private <T> TypeReferenceFrameHandler<T> getHandler(BlockingQueue<T> responseQueue,
                                                        TypeReference<T> typeReference) {
        return new TypeReferenceFrameHandler<>(responseQueue, typeReference);
    }
}
