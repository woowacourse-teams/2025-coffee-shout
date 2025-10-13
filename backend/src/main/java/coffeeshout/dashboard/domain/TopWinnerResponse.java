package coffeeshout.dashboard.domain;

public record TopWinnerResponse(
        String nickname,
        Long winCount
) {
}
