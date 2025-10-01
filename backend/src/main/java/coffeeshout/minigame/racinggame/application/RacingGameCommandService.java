package coffeeshout.minigame.racinggame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레이싱 게임의 비즈니스 로직을 처리하는 Command Service
 * 이벤트 핸들러에서 호출되며, 실제 게임 로직을 실행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RacingGameCommandService {

    private final RoomQueryService roomQueryService;

    /**
     * 레이싱 게임을 시작합니다.
     * 1. Room 상태를 PLAYING으로 변경
     * 2. RacingGame 시작 (자동 이동 스케줄러 시작)
     */
    public void startGame(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);

        // Room 상태 변경
        room.startNextGame(hostName);

        // 레이싱 게임 시작
        final RacingGame racingGame = getRacingGame(room);
        racingGame.startGame(room.getPlayers().getPlayers());

        log.info("레이싱 게임 시작 완료: joinCode={}", joinCode);
    }

    /**
     * 플레이어의 탭 입력을 처리하여 속도를 조정합니다.
     */
    public void processTap(String joinCode, String playerName, int tapCount, Instant timestamp) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);
        final Player player = room.getPlayers().findPlayerByName(new PlayerName(playerName));

        final RacingGame racingGame = getRacingGame(room);
        racingGame.adjustSpeed(player, tapCount, timestamp);

        log.debug("탭 처리 완료: joinCode={}, playerName={}, tapCount={}", joinCode, playerName, tapCount);
    }

    private RacingGame getRacingGame(Room room) {
        return (RacingGame) room.findMiniGame(MiniGameType.RACING_GAME);
    }
}
