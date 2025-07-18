package coffeeshout.minigame.ui;

public record CardGameSelectMessage(
        Long roomId,
        String playerName,
        Integer cardIndex
) {
}
