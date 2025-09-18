package coffeeshout.minigame.domain.cardgame.event.dto;

public record CardGameStartProcessEvent(
        String joinCode,
        String cardGameTaskType
) {
}
