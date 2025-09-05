package coffeeshout.room.domain.menu;

public class CustomMenu extends Menu {

    private final String categoryImageUrl;

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
        return 0L;
    }
}
