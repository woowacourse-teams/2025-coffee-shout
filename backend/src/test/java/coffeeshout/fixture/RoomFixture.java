package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.Players;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        final Room room = new Room(
                new JoinCode("A4B2C"),
                PlayerFixture.호스트꾹이().getName(),
                new SelectedMenu(MenuFixture.아메리카노(), MenuTemperature.ICE),
                0
        );
        final Players players = PlayersFixture.루키_엠제이_한스_리스트;
        int colorIndex = 1;
        for (Player player : players.getPlayers()) {
            room.joinGuest(player.getName(), player.getSelectedMenu(), colorIndex++);
        }
        return room;
    }
}
