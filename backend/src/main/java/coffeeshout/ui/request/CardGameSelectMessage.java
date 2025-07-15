package coffeeshout.ui.request;

public record CardGameSelectMessage(
        Long roomId,
        Long playerId,
        Integer cardPosition
) {
}
