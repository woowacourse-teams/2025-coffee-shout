package coffeeshout.room.ui;

import coffeeshout.common.ApiEnums;
import coffeeshout.common.ApiSchema;
import coffeeshout.common.EnumMapping;
import coffeeshout.common.MessageResponse;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.MenuType;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.request.MiniGameSelectMessage;
import coffeeshout.room.ui.request.ReadyChangeMessage;
import coffeeshout.room.ui.request.RouletteSpinMessage;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
class RoomWebSocketControllerTest extends WebSocketIntegrationTestSupport {

    JoinCode joinCode;
    
    Player host;
    
    TestStompSession session;
    
    Room testRoom;

    @BeforeEach
    void setUp(@Autowired RoomRepository roomRepository) throws Exception {
        testRoom = RoomFixture.호스트_꾹이();
        joinCode = testRoom.getJoinCode();  // Room에서 실제 joinCode 가져오기
        host = testRoom.getHost();
        
        roomRepository.save(testRoom);
        session = createSession();
    }

    @Test
    @ApiEnums({
            @EnumMapping(field = "menuType", enumClass = MenuType.class),
            @EnumMapping(field = "playerType", enumClass = PlayerType.class)
    })
    @ApiSchema(description = """
            [방에 입장하여 플레이어 목록을 조회한다]
            
            - /app/room/{joinCode}/update-players로 요청을 보내면 플레이어 목록을 받을 수 있다.
            - 응답으로는 방에 있는 모든 플레이어의 정보가 포함된다.
            
            """,
            responseType = PlayerResponse.class
    )
    void 플레이어_목록을_조회한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/update-players", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat);

        // then
        MessageResponse playersResponse = responses.get();
        assertMessage(playersResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerName":"꾹이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"HOST",
                         "isReady":true,
                         "colorIndex":0
                      },
                      {
                         "playerName":"루키",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":1
                      },
                      {
                         "playerName":"엠제이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":2
                      },
                      {
                         "playerName":"한스",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":3
                      }
                   ],
                   "errorMessage":null
                }
                """);
    }

    @Test
    @ApiSchema(description = """
            [플레이어의 메뉴를 변경한다]
            
            - /app/room/{joinCode}/update-menus로 메뉴 변경 요청을 보낸다.
            - 변경된 메뉴 정보가 모든 구독자에게 브로드캐스트된다.
            
            """,
            responseType = PlayerResponse.class,
            requestType = MenuChangeMessage.class
    )
    void 플레이어_메뉴를_변경한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/update-menus", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat, String.format("""
                {
                  "playerName": "한스",
                  "menuId": 2
                }
                """));

        // then
        MessageResponse menuChangeResponse = responses.get();
        
        assertMessage(menuChangeResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerName":"꾹이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"HOST",
                         "isReady":true,
                         "colorIndex":0
                      },
                      {
                         "playerName":"루키",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":1
                      },
                      {
                         "playerName":"엠제이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":2
                      },
                      {
                         "playerName":"한스",
                         "menuResponse":{
                            "id":2,
                            "name":"카페라떼",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":3
                      }
                   ],
                   "errorMessage":null
                }
                """);
    }

    @Test
    @ApiSchema(description = """
            [준비 상태를 변경한다]
            
            - /app/room/{joinCode}/update-ready로 준비 상태 변경 요청을 보낸다.
            - 변경된 준비 상태가 모든 구독자에게 브로드캐스트된다.
            
            """,
            responseType = PlayerResponse.class,
            requestType = ReadyChangeMessage.class
    )
    void 준비_상태를_변경한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/update-ready", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat, String.format("""
                {
                  "joinCode": "%s",
                  "playerName": "루키",
                  "isReady": false
                }
                """, joinCode.value()));

        // then
        MessageResponse readyResponse = responses.get();

        assertMessage(readyResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerName":"꾹이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"HOST",
                         "isReady":true,
                         "colorIndex":0
                      },
                      {
                         "playerName":"루키",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":1
                      },
                      {
                         "playerName":"엠제이",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":2
                      },
                      {
                         "playerName":"한스",
                         "menuResponse":{
                            "id":1,
                            "name":"아메리카노",
                            "menuType":"COFFEE"
                         },
                         "playerType":"GUEST",
                         "isReady":false,
                         "colorIndex":3
                      }
                   ],
                   "errorMessage":null
                }
                """);
    }

    @Test
    @ApiSchema(description = """
            [플레이어들의 확률을 조회한다]
            
            - /app/room/{joinCode}/get-probabilities로 확률 조회 요청을 보낸다.
            - 각 플레이어의 당첨 확률이 /topic/room/{joinCode}/roulette로 응답된다.
            
            """,
            responseType = ProbabilityResponse.class
    )
    void 플레이어들의_확률을_조회한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/roulette", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/get-probabilities", joinCode.value());

        var responses = session.subscribe(subscribeUrlFormat);

        // when
        session.send(requestUrlFormat);

        // then
        MessageResponse probabilityResponse = responses.get();

        assertMessage(probabilityResponse, """
                {
                   "success":true,
                   "data":[
                      {
                         "playerResponse":{
                            "playerName":"꾹이",
                            "menuResponse":{
                               "id":1,
                               "name":"아메리카노",
                               "menuType":"COFFEE"
                            },
                            "playerType":"HOST",
                            "isReady":true,
                            "colorIndex":0
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"루키",
                            "menuResponse":{
                               "id":1,
                               "name":"아메리카노",
                               "menuType":"COFFEE"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":1
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"엠제이",
                            "menuResponse":{
                               "id":1,
                               "name":"아메리카노",
                               "menuType":"COFFEE"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":2
                         },
                         "probability":25.0
                      },
                      {
                         "playerResponse":{
                            "playerName":"한스",
                            "menuResponse":{
                               "id":1,
                               "name":"아메리카노",
                               "menuType":"COFFEE"
                            },
                            "playerType":"GUEST",
                            "isReady":false,
                            "colorIndex":3
                         },
                         "probability":25.0
                      }
                   ],
                   "errorMessage":null
                }
                """);
    }

    @Test
    @ApiEnums({
            @EnumMapping(field = "miniGameType", enumClass = MiniGameType.class)
    })
    @ApiSchema(description = """
            [미니게임을 선택한다]
            
            - 호스트가 /app/room/{joinCode}/update-minigames로 미니게임 선택 요청을 보낸다.
            - 선택된 미니게임 목록이 모든 구독자에게 브로드캐스트된다.
            
            """,
            responseType = MiniGameType.class,
            requestType = MiniGameSelectMessage.class
    )
    void 미니게임을_선택한다() throws JSONException {
        // given
        String subscribeUrlFormat = String.format("/topic/room/%s/minigame", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/update-minigames", joinCode.value());

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
    @ApiSchema(description = """
            [룰렛을 돌려서 당첨자를 선택한다]
            
            - 방 상태가 PLAYING일 때만 룰렛을 돌릴 수 있다.
            - /app/room/{joinCode}/spin-roulette로 룰렛 회전 요청을 보낸다.
            - 선택된 당첨자 정보가 /topic/room/{joinCode}/winner로 브로드캐스트된다.
            
            """,
            responseType = PlayerResponse.class,
            requestType = RouletteSpinMessage.class
    )
    void 룰렛을_돌려서_당첨자를_선택한다() throws JSONException {
        // given
        ReflectionTestUtils.setField(testRoom, "roomState", RoomState.PLAYING);

        String subscribeUrlFormat = String.format("/topic/room/%s/winner", joinCode.value());
        String requestUrlFormat = String.format("/app/room/%s/spin-roulette", joinCode.value());

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
        assertMessageContains(winnerResponse, "\"menuResponse\":");
    }
}
