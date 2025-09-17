package coffeeshout.room.application.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.global.messaging.RedisStreamBroadcastService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import coffeeshout.support.test.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

@IntegrationTest
@DisplayName("RoomBroadcastStreamConsumer 통합 테스트")
class RoomBroadcastStreamConsumerTest {

    @Autowired
    private RoomBroadcastStreamConsumer consumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBroadcastStreamProducer producer;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Room testRoom;
    private String joinCode;

    @BeforeEach
    void setUp() {
        testRoom = RoomFixture.호스트_꾹이();
        roomRepository.save(testRoom);
        joinCode = testRoom.getJoinCode().getValue();
    }

    @Nested
    @DisplayName("메시지 수신 및 처리 테스트")
    class MessageProcessingTest {

        @Test
        @DisplayName("ENTER_ROOM_BROADCAST 메시지를 받으면 플레이어가 방에 추가된다")
        void onMessage_WithEnterRoomBroadcast_AddsPlayerToRoom() throws Exception {
            // given
            String playerName = "새로운게스트";
            SelectedMenuRequest menu = new SelectedMenuRequest(2L, "라떼", MenuTemperature.ICE);

            Map<String, Object> requestData = Map.of(
                    "joinCode", joinCode,
                    "playerName", playerName,
                    "selectedMenuRequest", menu
            );

            MapRecord<String, String, String> record = createBroadcastRecord(
                    "ENTER_ROOM_BROADCAST",
                    requestData
            );

            int initialPlayerCount = testRoom.getPlayers().size();

            // when
            consumer.onMessage(record);

            // then
            Room updatedRoom = roomRepository.findByJoinCode(new JoinCode(joinCode)).orElseThrow();
            assertThat(updatedRoom.getPlayers()).hasSize(initialPlayerCount + 1);
            assertThat(updatedRoom.getPlayers())
                    .extracting(player -> player.getName().value())
                    .contains(playerName);

        }

        @Test
        @DisplayName("UPDATE_ROOM_STATE 메시지를 받으면 처리 완료 응답을 반환한다")
        void onMessage_WithUpdateRoomState_ReturnsProcessedResponse() throws Exception {
            // given
            Map<String, Object> roomState = Map.of("status", "waiting", "playerCount", 2);
            MapRecord<String, String, String> record = createBroadcastRecord(
                    "UPDATE_ROOM_STATE",
                    Map.of("joinCode", joinCode, "roomState", roomState)
            );

            // when
            consumer.onMessage(record);

            // then
            // 완료 알림이 COMPLETION_STREAM으로 전송되었는지 확인
            Long completionStreamLength = stringRedisTemplate.opsForStream()
                    .size(RoomBroadcastStreamProducer.COMPLETION_STREAM);
            assertThat(completionStreamLength).isGreaterThan(0);
        }

        @Test
        @DisplayName("SYNC_ROOM_DATA 메시지를 받으면 데이터 동기화 처리를 수행한다")
        void onMessage_WithSyncRoomData_ProcessesDataSync() throws Exception {
            // given
            Map<String, Object> roomData = Map.of("players", 2, "gameState", "lobby");
            MapRecord<String, String, String> record = createBroadcastRecord(
                    "SYNC_ROOM_DATA",
                    Map.of("joinCode", joinCode, "roomData", roomData)
            );

            // when
            consumer.onMessage(record);

            // then
            // 처리 완료 응답이 전송되었는지 확인
            Long completionStreamLength = stringRedisTemplate.opsForStream()
                    .size(RoomBroadcastStreamProducer.COMPLETION_STREAM);
            assertThat(completionStreamLength).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("완료 알림 전송 테스트")
    class CompletionNotificationTest {

        @Test
        @DisplayName("처리 중 예외 발생 시 에러 응답을 전송한다")
        void messageProcessing_WithException_SendsErrorResponse() throws Exception {
            // given - 존재하지 않는 방 코드로 메시지 생성
            String invalidJoinCode = "XXXXX";
            MapRecord<String, String, String> record = createBroadcastRecord(
                    "ENTER_ROOM_BROADCAST",
                    Map.of(
                            "joinCode", invalidJoinCode,
                            "playerName", "테스트게스트",
                            "selectedMenuRequest", new SelectedMenuRequest(1L, "아메리카노", MenuTemperature.HOT)
                    )
            );

            // when
            consumer.onMessage(record);

            // then
            // 에러 응답이 RESPONSE_STREAM으로 전송되었는지 확인
            Long responseStreamLength = stringRedisTemplate.opsForStream()
                    .size(RedisStreamBroadcastService.RESPONSE_STREAM);
            assertThat(responseStreamLength).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("실제 비즈니스 로직 테스트")
    class BusinessLogicTest {

        @Test
        @DisplayName("플레이어 입장 처리 시 실제 Room 엔티티가 올바르게 업데이트된다")
        void enterRoomProcessing_UpdatesRoomEntityCorrectly() throws Exception {
            // given
            String playerName = "인원 추가";
            SelectedMenuRequest menu = new SelectedMenuRequest(4L, "에스프레소", MenuTemperature.ICE);

            producer.broadcastEnterRoom(joinCode, playerName, menu);

            // then
            await().atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> {
                        Room updatedRoom = roomRepository.findByJoinCode(new JoinCode(joinCode)).orElseThrow();
                        Player result = updatedRoom.getPlayers().stream()
                                .filter(player -> playerName.equals(player.getName().value())).findFirst()
                                .orElseThrow(() -> new IllegalStateException("플레이어가 추가되지 않음"));

                        assertThat(result.getSelectedMenu().menu().getId()).isEqualTo(menu.id());
                        assertThat(result.getSelectedMenu().menu().getName()).isEqualTo(menu.customName());
                        assertThat(result.getSelectedMenu().menuTemperature()).isEqualTo(menu.temperature());
                    });
        }

        @Test
        @DisplayName("여러 플레이어가 동시에 입장해도 모두 올바르게 처리된다")
        void multiplePlayersEntering_ProcessedCorrectly() {
            // given
            String[] playerNames = {"플레이어1", "플레이어2", "플레이어3"};
            SelectedMenuRequest[] menus = {
                    new SelectedMenuRequest(1L, "아메리카노", MenuTemperature.HOT),
                    new SelectedMenuRequest(2L, "카페라떼", MenuTemperature.ICE),
                    new SelectedMenuRequest(3L, "에스프레소", MenuTemperature.HOT)
            };

            for (int i = 0; i < playerNames.length; i++) {
                producer.broadcastEnterRoom(joinCode, playerNames[i], menus[i]);
            }

            // then
            await().atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> {
                        Room updatedRoom = roomRepository.findByJoinCode(new JoinCode(joinCode)).orElseThrow();
                        assertThat(updatedRoom.getPlayers())
                                .extracting(player -> player.getName().value())
                                .contains(playerNames);
                    });
        }
    }

    // 헬퍼 메서드들
    private MapRecord<String, String, String> createBroadcastRecord(String type, Map<String, Object> requestData)
            throws Exception {
        String requestId = UUID.randomUUID().toString();
        String sender = "other-instance-8080"; // 다른 인스턴스에서 보낸 것으로 설정

        return StreamRecords.mapBacked(Map.of(
                "requestId", requestId,
                "type", type,
                "data", objectMapper.writeValueAsString(requestData),
                "sender", sender,
                "timestamp", String.valueOf(System.currentTimeMillis())
        )).withStreamKey("test-stream");
    }

    private String getConsumerInstanceId() {
        // Consumer의 instanceId와 동일하게 생성
        return "app-8080"; // 기본값
    }
}
