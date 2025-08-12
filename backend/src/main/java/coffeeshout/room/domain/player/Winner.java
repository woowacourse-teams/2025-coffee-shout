package coffeeshout.room.domain.player;

import java.util.Random;

public record Winner(
        PlayerName name,
        Integer colorIndex,
        Integer randomAngle
) {

    private static final Random RANDOM = new Random();
    private static final int PIE_SEGMENT = 100;

    public static Winner from(Player player) {
        return new Winner(
                player.getName(),
                player.getColorIndex(),
                RANDOM.nextInt(PIE_SEGMENT)
        );
    }
}
