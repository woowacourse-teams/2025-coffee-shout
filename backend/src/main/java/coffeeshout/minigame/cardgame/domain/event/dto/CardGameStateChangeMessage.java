package coffeeshout.minigame.cardgame.domain.event.dto;

public record CardGameStateChangeMessage(
        String joinCode,
        String currentTaskName,
        long nextTaskStartMillis
) {
}
