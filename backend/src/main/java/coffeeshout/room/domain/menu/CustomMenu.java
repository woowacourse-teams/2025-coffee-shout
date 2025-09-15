package coffeeshout.room.domain.menu;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CustomMenu extends Menu {

    private String categoryImageUrl;

    public CustomMenu(String name, String categoryImageUrl) {
        super(name, TemperatureAvailability.BOTH);
        this.categoryImageUrl = categoryImageUrl;
    }

    @Override
    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    @Override
    public Long getId() {
        throw new IllegalStateException("CustomMenu는 id가 없습니다.");
    }
}
