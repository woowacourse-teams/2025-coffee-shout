package coffeeshout.minigame.ui;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.fixture.CardGameDeckStub;
import coffeeshout.fixture.CardGameFake;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.MessageResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.RoomCommandService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    JoinCode joinCode;

    Player host;

    TestStompSession session;

    CardGame cardGame;

    @Autowired
    RoomCommandService roomCommandService;

    @Autowired
    CardGameCommandService cardGameCommandService;

    @BeforeEach
    void setUp() throws Exception {
        joinCode = new JoinCode("A4B2C");
        Room room = RoomFixture.호스트_꾹이();
        room.getPlayers().forEach(player -> player.updateReadyState(true));
        host = room.getHost();
        cardGame = new CardGameFake(room.getPlayers(), joinCode, new CardGameDeckStub());
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME);
        roomCommandService.save(room);
        cardGameCommandService.save(cardGame);
        session = createSession();
    }

    @AfterEach
    void tearDown(@Autowired CardGameTaskExecutors cardGameTaskExecutors) {
        cardGameTaskExecutors.get(joinCode).cancelAll();
    }

    @Test
    void 카드게임을_실행한다() {
        // given
        String joinCodeValue = joinCode.getValue();
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCodeValue);
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCodeValue);

        var responses = session.subscribe(subscribeUrlFormat);

        session.send(
                requestUrlFormat, String.format(
                        """
                                {
                                  "commandType": "START_MINI_GAME",
                                  "commandRequest": {
                                    "hostName": "%s"
                                  }
                                }
                                """, host.getName().value()
                )
        );

        MessageResponse firstRoundLoading = responses.get();
        MessageResponse prepare = responses.get();
        MessageResponse firstRoundPlaying = responses.get();
        MessageResponse firstRoundScoreBoard = responses.get(11, TimeUnit.SECONDS);
        MessageResponse secondRoundLoading = responses.get();
        MessageResponse secondRoundPlaying = responses.get();
        MessageResponse secondRoundScoreBoard = responses.get(11, TimeUnit.SECONDS);
        MessageResponse done = responses.get();

        assertMessageContains(firstRoundLoading, "\"cardGameState\":\"FIRST_LOADING\"");
        assertMessageContains(firstRoundLoading, "\"currentRound\":\"FIRST\"");
        assertMessageContains(firstRoundLoading, "\"success\":true");
        assertMessageContains(prepare, 4000L, "\"cardGameState\":\"PREPARE\"");
        assertMessageContains(firstRoundPlaying, 2000L, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(firstRoundScoreBoard, 10250L, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(secondRoundLoading, 1500L, "\"cardGameState\":\"LOADING\"");
        assertMessageContains(secondRoundPlaying, 3000L, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(secondRoundScoreBoard, 10300L, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(done, "\"cardGameState\":\"DONE\"");
    }

    @Test
    void 카드를_선택한다() {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.getValue());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.getValue());

        var responses = session.subscribe(subscribeUrlFormat);

        session.send(
                requestUrlFormat, String.format(
                        """
                                {
                                  "commandType": "START_MINI_GAME",
                                  "commandRequest": {
                                    "hostName": "%s"
                                  }
                                }
                                """, host.getName().value()
                )
        );

        responses.get(); // FIRST_LOADING
        responses.get(); // PREPARE
        responses.get(); // PLAYING

        // when
        session.send(
                requestUrlFormat, """
                        {
                           "commandType": "SELECT_CARD",
                           "commandRequest": {
                             "playerName": "꾹이",
                             "cardIndex": 0
                           }
                        }
                        """
        );
        MessageResponse firstRoundPlaying = responses.get(10, TimeUnit.SECONDS);

        // then
        assertMessageContains(firstRoundPlaying, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(firstRoundPlaying, "\"currentRound\":\"FIRST\"");
        assertMessageContains(firstRoundPlaying, "\"success\":true");

        // 하나의 카드는 선택(꾹이)
        assertMessageContains(firstRoundPlaying, "\"playerName\":\"꾹이\"");
        assertMessageContains(firstRoundPlaying, "\"selected\":true");

        // 나머지 8개 카드는 선택되지 않음
        String responseContent = firstRoundPlaying.payload();
        long nullPlayerNameCount = responseContent.split("\"playerName\":null").length - 1;
        assertThat(nullPlayerNameCount).isEqualTo(8);


    }

    private static Customization getColorIndexCustomization() {
        return new Customization(
                "colorIndex", (actual, expect) -> {
            if (expect instanceof Integer value) {
                return value >= 0 && value <= 9;
            }
            return true;
        }
        );
    }
}
