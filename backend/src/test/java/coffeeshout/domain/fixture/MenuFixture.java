package coffeeshout.domain.fixture;

import coffeeshout.domain.Menu;

public class MenuFixture {

    public static Menu 아메리카노() {
        return new Menu("아메리카노", "sample-image1.png");
    }

    public static Menu 라떼() {
        return new Menu("라떼", "sample-image2.png");
    }

    public static Menu 아이스티() {
        return new Menu("아이스티", "sample-image3.png");
    }
}
