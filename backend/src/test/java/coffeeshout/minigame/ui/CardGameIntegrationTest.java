package coffeeshout.minigame.ui;

import coffeeshout.common.ApiEnums;
import coffeeshout.common.ApiSchema;
import coffeeshout.common.EnumMapping;
import coffeeshout.common.MessageResponse;
import coffeeshout.fixture.CardGameDeckStub;
import coffeeshout.fixture.CardGameFake;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import coffeeshout.minigame.ui.request.command.StartMiniGameCommand;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    JoinCode joinCode;

    Player host;

    TestStompSession session;

    CardGame cardGame;

    @BeforeEach
    void setUp(@Autowired RoomRepository roomRepository) throws Exception {
        joinCode = new JoinCode("A4B2C");
        Room room = RoomFixture.호스트_꾹이();
        host = room.getHost();
        cardGame = new CardGameFake(new CardGameDeckStub());
        room.addMiniGame(host.getName(), cardGame);
        roomRepository.save(room);
        session = createSession();
    }

    @AfterEach
    void tearDown(@Autowired CardGameTaskExecutors cardGameTaskExecutors) {
        cardGameTaskExecutors.get(joinCode).cancelAll();
    }

    @Test
    @ApiEnums({
            @EnumMapping(field = "cardGameState", enumClass = CardGameState.class),
            @EnumMapping(field = "cardType", enumClass = CardType.class),
            @EnumMapping(field = "currentRound", enumClass = CardGameRound.class)
    })
    @ApiSchema(description = """
            [미니게임을 시작한다]
            
            - /app/room/{joinCode}/minigame/command로 요청을 보내면 게임이 시작된다.
            - 게임이 시작되면 CardGameTaskType에 지정된 Task들을 이용해서 자동으로 카드게임의 태스크를 실행한다.
            - Task별로 카드게임을 실행한다. (Task마다 실행 시간이 존재한다.)
            
            """,
            responseType = MiniGameStateMessage.class,
            requestType = StartMiniGameCommand.class
    )
    void 카드게임을_실행한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        session.send(requestUrlFormat, String.format("""
                {
                  "commandType": "START_MINI_GAME",
                  "commandRequest": {
                    "hostName": "%s"
                  }
                }
                """, host.getName().value()));

        MessageResponse firstRoundLoading = responses.get();
        MessageResponse firstRoundDescription = responses.get();
        MessageResponse firstRoundPlaying = responses.get();
        MessageResponse firstRoundScoreBoard = responses.get(11, TimeUnit.SECONDS);
        MessageResponse secondRoundLoading = responses.get();
        MessageResponse secondRoundPlaying = responses.get();
        MessageResponse secondRoundScoreBoard = responses.get(11, TimeUnit.SECONDS);
        MessageResponse done = responses.get();

        assertMessage(firstRoundLoading, """
                {
                   "success":true,
                   "data":{
                      "cardGameState":"LOADING",
                      "currentRound":"FIRST",
                      "cardInfoMessages":[
                         {
                            "cardType":"ADDITION",
                            "value":40,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":30,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":20,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":10,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":0,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":-10,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":4,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":2,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":0,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         }
                      ],
                      "allSelected":false
                   },
                   "errorMessage":null
                }
                """);

        assertMessageContains(firstRoundDescription, 3000L, "\"cardGameState\":\"DESCRIPTION\"");
        assertMessageContains(firstRoundPlaying, 1500L, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(firstRoundScoreBoard, 10250L, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(secondRoundLoading, 1500L, "\"cardGameState\":\"LOADING\"");
        assertMessageContains(secondRoundPlaying, 3000L, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(secondRoundScoreBoard, 10250L, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(done, "\"cardGameState\":\"DONE\"");
    }

    @Test
    @DisplayName("""
            설명~~~
            """)
    void 카드를_선택한다() throws Exception {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        session.send(requestUrlFormat, String.format("""
                {
                  "commandType": "START_MINI_GAME",
                  "commandRequest": {
                    "hostName": "%s"
                  }
                }
                """, host.getName().value()));

        responses.get(); // LOADING
        responses.get(); // PREPARE
        responses.get(); // PLAYING

        // when
        session.send(requestUrlFormat, """
                {
                   "commandType": "SELECT_CARD",
                   "commandRequest": {
                     "playerName": "꾹이",
                     "cardIndex": 0
                   }
                }
                """);
        MessageResponse firstRoundPlaying = responses.get();

        // then
        assertMessage(firstRoundPlaying, """
                {
                   "success":true,
                   "data":{
                      "cardGameState":"PLAYING",
                      "currentRound":"FIRST",
                      "cardInfoMessages":[
                         {
                            "cardType":"ADDITION",
                            "value":40,
                            "selected":true,
                            "playerName":"꾹이",
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":30,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":20,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":10,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":0,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"ADDITION",
                            "value":-10,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":4,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":2,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         },
                         {
                            "cardType":"MULTIPLIER",
                            "value":0,
                            "selected":false,
                            "playerName":null,
                            "colorIndex":null
                         }
                      ],
                      "allSelected":false
                   },
                   "errorMessage":null
                }
                """);
    }
}
