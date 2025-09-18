package coffeeshout.minigame.domain.dto;

public record CardGameStateDoneEvent(
        String joinCode,
        String cardGameTaskType
) {
}
