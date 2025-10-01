package coffeeshout.minigame.cardgame.domain.cardgame.event.dto;

public record CardGameStartMessage(
        String joinCode,
        String cardGameTaskType
) {
}
