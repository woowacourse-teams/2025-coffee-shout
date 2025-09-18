package coffeeshout.minigame.domain.cardgame.event.dto;

public record CardGameStateDoneEvent(
        String joinCode,
        String currentTaskName
) {
}
