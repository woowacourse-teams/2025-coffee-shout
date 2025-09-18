package coffeeshout.minigame.domain.cardgame.event.dto;

public record CardGameStartMessage(
        String joinCode,
        String cardGameTaskType
) {
}
