package coffeeshout.room.domain.menu;

import lombok.Getter;

@Getter
public class MenuCategory {

    private Long id;
    private final String name;
    private final String imageUrl;

    public MenuCategory(Long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
