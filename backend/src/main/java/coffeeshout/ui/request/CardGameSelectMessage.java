package coffeeshout.ui.request;

public record CardGameSelectMessage(
        Long roomId,
        String playerName,
        Integer cardIndex
) {
}
