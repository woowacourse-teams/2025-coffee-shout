package coffeeshout.minigame.racinggame.ui;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.MessageResponse;
import coffeeshout.racinggame.domain.RacingGame;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RacingGameIntegrationTest extends WebSocketIntegrationTestSupport {

    JoinCode joinCode;
    Player host;
    TestStompSession session;
    RacingGame racingGame;

    @BeforeEach
    void setUp(@Autowired RoomRepository roomRepository) throws Exception {
        joinCode = new JoinCode("A4BX");
        Room room = RoomFixture.호스트_꾹이();
        room.getPlayers().forEach(player -> player.updateReadyState(true));
        host = room.getHost();
        racingGame = new RacingGame();
        room.addMiniGame(new PlayerName(host.getName().value()), racingGame);
        roomRepository.save(room);
        session = createSession();
    }

    @Test
    void 레이싱_게임을_시작한다() {
        // given
        String joinCodeValue = joinCode.getValue();
        String subscribeStateUrl = String.format("/topic/room/%s/racing-game/state", joinCodeValue);
        String subscribePositionUrl = String.format("/topic/room/%s/racing-game", joinCodeValue);
        String requestUrl = String.format("/app/room/%s/racing-game/start", joinCodeValue);

        var stateResponses = session.subscribe(subscribeStateUrl);
        var positionResponses = session.subscribe(subscribePositionUrl);

        // when
        session.send(requestUrl, String.format("""
                {
                  "hostName": "%s"
                }
                """, host.getName().value()));

        MessageResponse gameStarted = stateResponses.get(3, TimeUnit.SECONDS);

        // then
        assertMessageContains(gameStarted, "\"state\":\"PLAYING\"");
        assertMessageContains(gameStarted, "\"success\":true");

        // 자동 이동으로 위치 업데이트 메시지가 계속 발행됨
        MessageResponse positionUpdate1 = positionResponses.get(1, TimeUnit.SECONDS);
        assertMessageContains(positionUpdate1, "\"position\"");
        assertMessageContains(positionUpdate1, "\"distance\"");
    }

    @Test
    void 탭을_처리하면_러너_위치가_업데이트된다() {
        // given
        String joinCodeValue = joinCode.getValue();
        String subscribePositionUrl = String.format("/topic/room/%s/racing-game", joinCodeValue);
        String startRequestUrl = String.format("/app/room/%s/racing-game/start", joinCodeValue);
        String tapRequestUrl = String.format("/app/room/%s/racinggame/tap", joinCodeValue);

        var stateResponses = session.subscribe(String.format("/topic/room/%s/racing-game/state", joinCodeValue));
        var positionResponses = session.subscribe(subscribePositionUrl);

        // 게임 시작
        session.send(startRequestUrl, String.format("""
                {
                  "hostName": "%s"
                }
                """, host.getName().value()));

        stateResponses.get(3, TimeUnit.SECONDS); // PLAYING 상태 대기

        // when - 탭 전송
        session.send(tapRequestUrl, """
                {
                  "playerName": "루키",
                  "tapCount": 5
                }
                """);

        // then - 위치 업데이트 메시지 확인
        MessageResponse positionUpdate = positionResponses.get(1, TimeUnit.SECONDS);
        assertMessageContains(positionUpdate, "\"position\"");
        assertMessageContains(positionUpdate, "\"success\":true");
    }

    @Test
    void 게임이_완주되면_FINISHED_상태가_전송된다(@Autowired RoomRepository roomRepository) throws Exception {
        // given - 2인 플레이어 방 생성 (빠른 완주를 위해 최소 인원)
        JoinCode singlePlayerJoinCode = new JoinCode("A4BX");
        Room singlePlayerRoom = new Room(
                singlePlayerJoinCode,
                new PlayerName("솔로"),
                new SelectedMenu(MenuFixture.아메리카노(), MenuTemperature.ICE)
        );
        // 최소 2명 필요 - 게스트 1명 추가
        singlePlayerRoom.joinGuest(new PlayerName("게스트"), new SelectedMenu(MenuFixture.아메리카노(), MenuTemperature.ICE));
        singlePlayerRoom.getPlayers().forEach(player -> player.updateReadyState(true));
        RacingGame singlePlayerGame = new RacingGame();
        singlePlayerRoom.addMiniGame(new PlayerName("솔로"), singlePlayerGame);
        roomRepository.save(singlePlayerRoom);

        TestStompSession singleSession = createSession();
        String joinCodeValue = singlePlayerJoinCode.getValue();
        String subscribeStateUrl = String.format("/topic/room/%s/racing-game/state", joinCodeValue);
        String startRequestUrl = String.format("/app/room/%s/racing-game/start", joinCodeValue);
        String tapRequestUrl = String.format("/app/room/%s/racing-game/tap", joinCodeValue);

        var stateResponses = singleSession.subscribe(subscribeStateUrl);

        // 게임 시작
        singleSession.send(startRequestUrl, """
                {
                  "hostName": "솔로"
                }
                """);

        MessageResponse gameStarted = stateResponses.get(3, TimeUnit.SECONDS);
        assertMessageContains(gameStarted, "\"state\":\"PLAYING\"");

        // when - 대량 탭으로 빠르게 완주 (두 플레이어 모두)
        for (int i = 0; i < 50; i++) {
            singleSession.send(tapRequestUrl, """
                    {
                      "playerName": "솔로",
                      "tapCount": 10
                    }
                    """);
            singleSession.send(tapRequestUrl, """
                    {
                      "playerName": "게스트",
                      "tapCount": 10
                    }
                    """);
            Thread.sleep(50);
        }

        // then - DONE 상태 확인 (최대 15초 대기)
        MessageResponse finishedState = stateResponses.get(15, TimeUnit.SECONDS);
        assertMessageContains(finishedState, "\"state\":\"DONE\"");
        assertMessageContains(finishedState, "\"success\":true");
    }
}
