package coffeeshout.minigame.domain.cardgame.event.dto;

public record CardGameStateChangeMessage(
        String joinCode,
        String currentTaskName
) {
}
