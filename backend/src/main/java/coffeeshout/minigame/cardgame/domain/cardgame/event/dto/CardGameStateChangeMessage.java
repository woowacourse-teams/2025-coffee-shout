package coffeeshout.minigame.cardgame.domain.cardgame.event.dto;

public record CardGameStateChangeMessage(
        String joinCode,
        String currentTaskName,
        long nextTaskStartMillis
) {
}
