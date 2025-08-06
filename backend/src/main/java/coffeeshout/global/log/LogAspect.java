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
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    public static final Marker NOTIFICATION_MARKER = MarkerFactory.getMarker("[NOTIFICATION]");

    @AfterReturning(
            value = "execution(* coffeeshout.room.application.RoomService.createRoom(..))",
            returning = "room"
    )
    public void logRoomCreation(Room room) {
        log.info(NOTIFICATION_MARKER, "JoinCode[{}] 방 생성 완료 - host: {}, createdAt: {}",
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
        log.info(NOTIFICATION_MARKER, "JoinCode[{}] 룰렛 추첨 완료 - 당첨자: {}",
                joinCode,
                player.getName().value());
    }

    @After(
            value = "execution(* coffeeshout.room.domain.repository.RoomRepository.deleteByJoinCode(..)) && args(joinCode)"
    )
    public void logDelayCleanUp(JoinCode joinCode) {
        log.info("JoinCode[{}] 방 삭제 완료", joinCode.value());
    }

    @AfterReturning(
            value = "execution(* coffeeshout.room.application.RoomService.enterRoom(..)) && args(joinCode, guestName, menuId)",
            returning = "room",
            argNames = "joinCode,guestName,menuId,room"
    )
    public void logEnterRoom(String joinCode, String guestName, Long menuId, Room room) {
        final List<String> playerNames = room.getPlayers().stream()
                .map(player -> player.getName().value())
                .toList();
        log.info("JoinCode[{}] 게스트 입장 - 게스트 이름: {}, 메뉴 ID: {}, 현재 참여자 목록: {}", joinCode, guestName, menuId,
                playerNames);
    }
}
