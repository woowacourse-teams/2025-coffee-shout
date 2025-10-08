package coffeeshout.minigame.cardgame.infra.messaging.handler;

import coffeeshout.minigame.cardgame.application.MiniGameService;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.domain.event.MiniGameEventType;
import coffeeshout.minigame.cardgame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartMiniGameCommandEventHandler implements MiniGameEventHandler<StartMiniGameCommandEvent> {

    private final Map<MiniGameType, MiniGameService> miniGameServiceMap;
    private final RoomQueryService roomQueryService;

    public StartMiniGameCommandEventHandler(
            RoomQueryService roomQueryService,
            List<MiniGameService> miniGameServices
    ) {
        this.roomQueryService = roomQueryService;
        this.miniGameServiceMap = new EnumMap<>(MiniGameType.class);
        miniGameServices.forEach(miniGameService -> miniGameServiceMap.put(
                miniGameService.getMiniGameType(),
                miniGameService
        ));
    }

    @Override
    public void handle(StartMiniGameCommandEvent event) {
        try {
            log.info("미니게임 시작 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            updateRoomStateAndStartGame(event);

        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    private void updateRoomStateAndStartGame(StartMiniGameCommandEvent event) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
        final Playable playable = room.startNextGame(event.hostName());
        miniGameServiceMap.get(playable.getMiniGameType()).start(event.joinCode(), event.hostName());
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
