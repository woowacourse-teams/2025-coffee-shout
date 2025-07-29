package coffeeshout.room.domain.player;

import lombok.Getter;

@Getter
public class Menu {

    private Long id;
    private final String name;
    private final MenuType menuType;

    public Menu(String name, MenuType menuType) {
        this.name = name;
        this.menuType = menuType;
    }

    public void setId(long id) {
        this.id = id;
    }
}
