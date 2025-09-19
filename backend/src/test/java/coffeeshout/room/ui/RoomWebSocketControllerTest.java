package coffeeshout.room.ui;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.MessageResponse;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.RoomCommandService;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class RoomWebSocketControllerTest extends WebSocketIntegrationTestSupport {

    @Autowired
    RoomCommandService roomCommandService;

    JoinCode joinCode;

    Player host;

    TestStompSession session;

    Room testRoom;

    @BeforeEach
    void setUp() throws Exception {
        testRoom = RoomFixture.호스트_꾹이();
        joinCode = testRoom.getJoinCode();  // Room에서 실제 joinCode 가져오기
        host = testRoom.getHost();

        roomCommandService.save(testRoom);
        session = createSession();
    }

    @Test
    void 플레이어_목록을_조회한다() throws JSONException {
        // given
        String joinCodeValue = joinCode.getValue();
        String subscribeUrlFormat = String.format("/topic/room/%s", joinCodeValue);
        String requestUrlFormat = String.format("/app/room/%s/update-players", joinCodeValue);

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat);

        // then
        MessageResponse playersResponse = responses.get();
        assertMessageCustomization(playersResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerName":"꾹이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"HOST",
                         "isReady":true,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"루키",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"엠제이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"한스",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      }
                   ],
                   "errorMessage":null
                }
                """, getColorIndexCustomization("colorIndex"));
    }

    @Test
    void 준비_상태를_변경한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s", joinCode.getValue());
        String requestUrlFormat = String.format("/app/room/%s/update-ready", joinCode.getValue());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat, String.format("""
                {
                  "joinCode": "%s",
                  "playerName": "루키",
                  "isReady": false
                }
                """, joinCode.getValue()));

        // then
        MessageResponse readyResponse = responses.get();

        assertMessageCustomization(readyResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerName":"꾹이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"HOST",
                         "isReady":true,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"루키",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"엠제이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      },
                      {
                         "playerName":"한스",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "temperature":"ICE",
                            "categoryImageUrl":"커피.jpg"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":"*",
                         "probability": 25
                      }
                   ],
                   "errorMessage":null
                }
                """, getColorIndexCustomization("colorIndex"));
    }

    @Test
    void 플레이어들의_확률을_조회한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/roulette", joinCode.getValue());
        String requestUrlFormat = String.format("/app/room/%s/get-probabilities", joinCode.getValue());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat);

        // then
        MessageResponse probabilityResponse = responses.get();

        assertMessageCustomization(probabilityResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerResponse":{
                            "playerName":"꾹이",
                            "menuResponse":{
                                "id":1,
                                "name":"아메리카노",
                                "temperature":"ICE",
                                "categoryImageUrl":"커피.jpg"
                            },
                            "playerType":"HOST",
                            "isReady":true,
                            "colorIndex":"*",
                            "probability": 25
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"루키",
                            "menuResponse":{
                                "id":1,
                                "name":"아메리카노",
                                "temperature":"ICE",
                                "categoryImageUrl":"커피.jpg"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":"*",
                            "probability": 25
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"엠제이",
                            "menuResponse":{
                                "id":1,
                                "name":"아메리카노",
                                "temperature":"ICE",
                                "categoryImageUrl":"커피.jpg"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":"*",
                            "probability": 25
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"한스",
                            "menuResponse":{
                                "id":1,
                                "name":"아메리카노",
                                "temperature":"ICE",
                                "categoryImageUrl":"커피.jpg"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":"*",
                            "probability": 25
                         },
                         "probability":25.0
                      }
                   ],
                   "errorMessage":null
                }
                """, getColorIndexCustomization("playerResponse.colorIndex"));
    }

    @Test
    void 미니게임을_선택한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/minigame", joinCode.getValue());
        String requestUrlFormat = String.format("/app/room/%s/update-minigames", joinCode.getValue());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat, String.format("""
                {
                  "hostName": "%s",
                  "miniGameTypes": ["CARD_GAME"]
                }
                """, host.getName().value()));

        // then
        MessageResponse miniGameResponse = responses.get();

        assertMessage(miniGameResponse, """
                {
                   "success":true,
                   "data":["CARD_GAME"],
                   "errorMessage":null
                }
                """);
    }

    @Test
    void 룰렛을_돌려서_당첨자를_선택한다() {
        // given
        ReflectionTestUtils.setField(testRoom, "roomState", RoomState.PLAYING);
        roomCommandService.save(testRoom);

        String subscribeUrlFormat = String.format("/topic/room/%s/winner", joinCode.getValue());
        String requestUrlFormat = String.format("/app/room/%s/spin-roulette", joinCode.getValue());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat, String.format("""
                {
                  "hostName": "%s"
                }
                """, host.getName().value()));

        // then
        MessageResponse winnerResponse = responses.get();

        // 룰렛 결과는 랜덤이므로 assertMessageContains 사용
        assertMessageContains(winnerResponse, "\"success\":true");
        assertMessageContains(winnerResponse, "\"playerName\":");
    }

    private static Customization getColorIndexCustomization(String path) {
        return new Customization(path, (actual, expect) -> {
            if (expect instanceof Integer value) {
                return value >= 0 && value <= 9;
            }
            return true;
        });
    }
}
