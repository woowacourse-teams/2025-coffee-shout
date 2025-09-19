package coffeeshout.room.application.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.infra.messaging.RoomBroadcastStreamProducer;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import coffeeshout.support.test.IntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("RoomBroadcastStreamProducer 통합 테스트")
class RoomBroadcastStreamProducerTest {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoomBroadcastStreamProducer producer;

    private String joinCode;

    @BeforeEach
    void setUp() {
        final Room testRoom = RoomFixture.호스트_꾹이();
        roomRepository.save(testRoom);
        joinCode = testRoom.getJoinCode().getValue();
    }

    @Nested
    @DisplayName("실제 비즈니스 로직 테스트")
    class BusinessLogicTest {

        @Test
        @DisplayName("플레이어 입장 처리 시 실제 Room 엔티티가 올바르게 업데이트된다")
        void enterRoomProcessing_UpdatesRoomEntityCorrectly() {
            // given
            String playerName = "인원 추가";
            SelectedMenuRequest menu = new SelectedMenuRequest(4L, "에스프레소", MenuTemperature.ICE);

            producer.broadcastEnterRoom(RoomJoinEvent.create(joinCode, playerName, menu));

            // then
            await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(100))
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
                RoomJoinEvent roomJoinEvent = RoomJoinEvent.create(joinCode, playerNames[i], menus[i]);
                producer.broadcastEnterRoom(roomJoinEvent);
            }

            // then
            await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(100))
                    .untilAsserted(() -> {
                        Room updatedRoom = roomRepository.findByJoinCode(new JoinCode(joinCode)).orElseThrow();
                        assertThat(updatedRoom.getPlayers())
                                .extracting(player -> player.getName().value())
                                .contains(playerNames);
                    });
        }
    }
}
