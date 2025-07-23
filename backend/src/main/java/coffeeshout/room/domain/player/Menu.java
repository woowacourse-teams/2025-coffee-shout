package coffeeshout.room.domain.player;

import lombok.Getter;

@Getter
public class Menu {

    private Long id;
    private final String name;
    private final String image;

    public Menu(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public void setId(long id) {
        this.id = id;
    }
}
