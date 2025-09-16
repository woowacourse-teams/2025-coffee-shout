package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.application.MiniGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.StartMiniGameCommand;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Playable;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartMiniGameCommandHandler implements MiniGameCommandHandler<StartMiniGameCommand> {

    private final RoomService roomService;
    private final Map<MiniGameType, MiniGameService> services;

    @Autowired
    public StartMiniGameCommandHandler(
            RoomService roomService,
            CardGameService cardGameService
    ) {
        services = new EnumMap<>(MiniGameType.class);
        services.put(MiniGameType.CARD_GAME, cardGameService);
        this.roomService = roomService;
    }

    @Override
    public void handle(String joinCode, StartMiniGameCommand command) {
        // RoomService를 통해 게임 시작 (저장 + 이벤트 발행 포함)
        final Playable currentGame = roomService.startMiniGame(joinCode, command.hostName());
        final MiniGameType miniGameType = currentGame.getMiniGameType();
        
        // 구체적인 미니게임 서비스 실행
        services.get(miniGameType).start(currentGame, joinCode);
    }

    @Override
    public Class<StartMiniGameCommand> getCommandType() {
        return StartMiniGameCommand.class;
    }
}
