package coffeeshout.room.domain.player;

import coffeeshout.room.domain.Room;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Embedded
    private PlayerName name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Menu menu;

    public Player(PlayerName name) {
        this.name = name;
    }

    public Player(PlayerName name, Menu menu) {
        this.name = name;
        this.menu = menu;
    }

    public void assignRoom(Room room) {
        this.room = room;
    }

    public void selectMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Player player)) {
            return false;
        }
        return Objects.equals(name, player.name);
    }

    public boolean sameName(PlayerName playerName) {
        return Objects.equals(name, playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
