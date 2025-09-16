package coffeeshout.minigame.domain.dto;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;

/**
 * 미니게임 완료를 알리는 로컬 이벤트
 * 이 이벤트를 받은 리스너에서 Redis로 동기화 이벤트를 발행함
 */
public record MiniGameCompletedLocalEvent(
    JoinCode joinCode,
    MiniGameType miniGameType,
    MiniGameResult result
) {}
