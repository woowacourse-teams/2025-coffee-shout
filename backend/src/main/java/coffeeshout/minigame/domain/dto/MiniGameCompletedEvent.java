package coffeeshout.minigame.domain.dto;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.JoinCode;
import lombok.NonNull;

public record MiniGameCompletedEvent(@NonNull JoinCode joinCode, @NonNull MiniGameResult miniGameResult) {
}
