package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.application.MiniGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.StartMiniGameCommand;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartMiniGameCommandHandler implements MiniGameCommandHandler<StartMiniGameCommand> {

    private final RoomQueryService roomQueryService;
    private final Map<MiniGameType, MiniGameService> services;

    @Autowired
    public StartMiniGameCommandHandler(
            RoomQueryService roomQueryService,
            CardGameService cardGameService
    ) {
        services = new EnumMap<>(MiniGameType.class);
        services.put(MiniGameType.CARD_GAME, cardGameService);
        this.roomQueryService = roomQueryService;
    }

    @Override
    public void handle(String joinCode, StartMiniGameCommand command) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));
        final Playable currentGame = room.startNextGame(command.hostName());
        final MiniGameType miniGameType = currentGame.getMiniGameType();
        services.get(miniGameType).start(currentGame, joinCode);
    }

    @Override
    public Class<StartMiniGameCommand> getCommandType() {
        return StartMiniGameCommand.class;
    }
}
