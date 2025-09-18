package coffeeshout.minigame.domain.dto;

public record CardGameStartProcessEvent(
        String joinCode,
        String cardGameTaskType
) {
}
