package coffeeshout.Fixture;

import coffeeshout.domain.Player;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class PlayerFixture {

    public static List<Player> getPlayers() {
        final Player rookie = new Player("루키");
        final Player mj = new Player("엠제이");
        final Player hans = new Player("한스");
        final Player gguk = new Player("꾹이");

        ReflectionTestUtils.setField(rookie, "id", 1L);
        ReflectionTestUtils.setField(mj, "id", 2L);
        ReflectionTestUtils.setField(hans, "id", 3L);
        ReflectionTestUtils.setField(gguk, "id", 4L);

        return List.of(
                rookie,
                mj,
                hans,
                gguk
        );
    }
}
