package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.menu.ProvidedMenu;
import coffeeshout.room.domain.menu.MenuCategory;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.TemperatureAvailability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void Redis에_Room을_저장하고_조회할_수_있다() {
        // Given
        JoinCode joinCode = new JoinCode("ABCDE");
        PlayerName hostName = new PlayerName("테스트호스트");
        MenuCategory menuCategory = new MenuCategory(1L, "커피", "coffee.png");
        ProvidedMenu menu = new ProvidedMenu(1L, "아메리카노", menuCategory, TemperatureAvailability.BOTH);
        SelectedMenu selectedMenu = new SelectedMenu(menu, MenuTemperature.HOT);
        Room room = Room.createNewRoom(joinCode, hostName, selectedMenu);

        // When
        Room savedRoom = roomRepository.save(room);

        // Then
        assertThat(savedRoom.getJoinCode()).isEqualTo(joinCode);
        assertThat(roomRepository.existsByJoinCode(joinCode)).isTrue();
        assertThat(roomRepository.findByJoinCode(joinCode)).isPresent();
    }

    @Test
    void JoinCode로_존재여부를_확인할_수_있다() {
        // Given
        JoinCode joinCode = new JoinCode("FGHJK");
        PlayerName hostName = new PlayerName("테스트호스트2");
        MenuCategory menuCategory = new MenuCategory(1L, "커피", "coffee.png");
        ProvidedMenu menu = new ProvidedMenu(1L, "아메리카노", menuCategory, TemperatureAvailability.BOTH);
        SelectedMenu selectedMenu = new SelectedMenu(menu, MenuTemperature.HOT);
        Room room = Room.createNewRoom(joinCode, hostName, selectedMenu);

        // When & Then
        assertThat(roomRepository.existsByJoinCode(joinCode)).isFalse();
        roomRepository.save(room);
        assertThat(roomRepository.existsByJoinCode(joinCode)).isTrue();
    }

    @Test
    void JoinCode로_방을_삭제할_수_있다() {
        // Given
        JoinCode joinCode = new JoinCode("KLMNP");
        PlayerName hostName = new PlayerName("테스트호스트3");
        MenuCategory menuCategory = new MenuCategory(1L, "커피", "coffee.png");
        ProvidedMenu menu = new ProvidedMenu(1L, "아메리카노", menuCategory, TemperatureAvailability.BOTH);
        SelectedMenu selectedMenu = new SelectedMenu(menu, MenuTemperature.HOT);
        Room room = Room.createNewRoom(joinCode, hostName, selectedMenu);

        roomRepository.save(room);
        assertThat(roomRepository.existsByJoinCode(joinCode)).isTrue();

        // When
        roomRepository.deleteByJoinCode(joinCode);

        // Then
        assertThat(roomRepository.existsByJoinCode(joinCode)).isFalse();
        assertThat(roomRepository.findByJoinCode(joinCode)).isEmpty();
    }
}