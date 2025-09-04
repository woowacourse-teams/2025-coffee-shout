package coffeeshout.room.domain.menu;

import lombok.Getter;

@Getter
public class MenuCategory {

    private Long id;
    private String name;

    public MenuCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
