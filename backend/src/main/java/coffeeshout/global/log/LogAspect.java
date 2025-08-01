package coffeeshout.global.log;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @AfterReturning(
            value = "execution(* coffeeshout.room.application.RoomService.createRoom(..))",
            returning = "room"
    )
    public void logRoomCreation(Room room) {
        log.info("JoinCode[{}] 방 생성 완료 - host: {}, createdAt: {}",
                room.getJoinCode().value(),
                room.getHost().getName().value(),
                LocalDateTime.now());
    }

    @After(
            value = "execution(* coffeeshout.minigame.application.MiniGameService.start(..)) && args(playable, joinCode)",
            argNames = "playable,joinCode"
    )
    public void logMiniGameStart(Playable playable, String joinCode) {
        log.info("JoinCode[{}] 미니게임 시작됨 - MiniGameType : {}", joinCode, playable.getMiniGameType());
    }

    @AfterReturning(
            value = "execution(* coffeeshout.room.application.RoomService.spinRoulette(..)) && args(joinCode, hostName)",
            returning = "player",
            argNames = "joinCode,hostName,player"
    )
    public void logSpinRoulette(String joinCode, String hostName, Player player) {
        log.info("JoinCode[{}] 룰렛 추첨 완료 - 당첨자: {}",
                joinCode,
                player.getName().value());
    }

    @After(
            value = "execution(* coffeeshout.room.domain.repository.RoomRepository.deleteByJoinCode(..)) && args(joinCode)"
    )
    public void logDelayCleanUp(JoinCode joinCode) {
        log.info("JoinCode[{}] 방 삭제 완료", joinCode.value());
    }
}
