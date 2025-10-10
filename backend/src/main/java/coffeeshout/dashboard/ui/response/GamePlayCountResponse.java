package coffeeshout.dashboard.ui.response;

import coffeeshout.minigame.domain.MiniGameType;

public record GamePlayCountResponse(
        MiniGameType gameType,
        Long playCount
) {
}
