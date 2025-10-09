package coffeeshout.minigame.cardgame.domain.event.dto;

public record CardGameStartMessage(
        String joinCode,
        String cardGameTaskType
) {
}
