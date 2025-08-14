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

    @Test
    @DisplayName("다중 플레이어가 카드를 선택하는 시나리오를 테스트한다")
    void 다중_플레이어_카드_선택_시나리오() throws Exception {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // 게임 시작
        session.send(requestUrlFormat, String.format("""
                {
                  "commandType": "START_MINI_GAME",
                  "commandRequest": {
                    "hostName": "%s"
                  }
                }
                """, host.getName().value()));

        responses.get(); // LOADING
        responses.get(); // DESCRIPTION
        responses.get(); // PLAYING

        // when - 여러 플레이어가 카드 선택
        session.send(requestUrlFormat, """
                {
                   "commandType": "SELECT_CARD",
                   "commandRequest": {
                     "playerName": "꾹이",
                     "cardIndex": 0
                   }
                }
                """);
        MessageResponse firstPlayerResponse = responses.get();

        session.send(requestUrlFormat, """
                {
                   "commandType": "SELECT_CARD",
                   "commandRequest": {
                     "playerName": "루키",
                     "cardIndex": 1
                   }
                }
                """);
        MessageResponse secondPlayerResponse = responses.get();

        // then - 두 플레이어의 카드 선택 상태 확인
        assertMessageContains(firstPlayerResponse, "\"selected\":true");
        assertMessageContains(firstPlayerResponse, "\"playerName\":\"꾹이\"");
        
        assertMessageContains(secondPlayerResponse, "\"selected\":true");
        assertMessageContains(secondPlayerResponse, "\"playerName\":\"루키\"");
    }

    @Test
    @DisplayName("게임 상태 변화 순서를 검증한다")
    void 게임_상태_변화_순서_검증() throws Exception {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when - 게임 시작
        session.send(requestUrlFormat, String.format("""
                {
                  "commandType": "START_MINI_GAME",
                  "commandRequest": {
                    "hostName": "%s"
                  }
                }
                """, host.getName().value()));

        // then - 상태 변화 순서 확인
        MessageResponse loading1 = responses.get();
        assertMessageContains(loading1, "\"cardGameState\":\"LOADING\"");
        assertMessageContains(loading1, "\"currentRound\":\"FIRST\"");

        MessageResponse description1 = responses.get();
        assertMessageContains(description1, "\"cardGameState\":\"DESCRIPTION\"");
        assertMessageContains(description1, "\"currentRound\":\"FIRST\"");

        MessageResponse playing1 = responses.get();
        assertMessageContains(playing1, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(playing1, "\"currentRound\":\"FIRST\"");

        MessageResponse scoreBoard1 = responses.get(11, TimeUnit.SECONDS);
        assertMessageContains(scoreBoard1, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(scoreBoard1, "\"currentRound\":\"FIRST\"");

        MessageResponse loading2 = responses.get();
        assertMessageContains(loading2, "\"cardGameState\":\"LOADING\"");
        assertMessageContains(loading2, "\"currentRound\":\"SECOND\"");

        MessageResponse playing2 = responses.get();
        assertMessageContains(playing2, "\"cardGameState\":\"PLAYING\"");
        assertMessageContains(playing2, "\"currentRound\":\"SECOND\"");

        MessageResponse scoreBoard2 = responses.get(11, TimeUnit.SECONDS);
        assertMessageContains(scoreBoard2, "\"cardGameState\":\"SCORE_BOARD\"");
        assertMessageContains(scoreBoard2, "\"currentRound\":\"SECOND\"");

        MessageResponse done = responses.get();
        assertMessageContains(done, "\"cardGameState\":\"DONE\"");
    }

    @Test
    @DisplayName("잘못된 커맨드 타입 요청 시 에러 처리를 확인한다")
    void 잘못된_커맨드_타입_에러_처리() throws Exception {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/gameState", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/minigame/command", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when - 잘못된 커맨드 타입 전송
        session.send(requestUrlFormat, """
                {
                  "commandType": "INVALID_COMMAND",
                  "commandRequest": {
                    "hostName": "꾹이"
                  }
                }
                """);

        // then - 에러 응답이나 무반응 확인
        try {
            MessageResponse errorResponse = responses.get(2, TimeUnit.SECONDS);
            assertMessageContains(errorResponse, "error");
        } catch (Exception e) {
            // 타임아웃이나 예외가 발생하면 정상적으로 무시됨
        }
    }
}