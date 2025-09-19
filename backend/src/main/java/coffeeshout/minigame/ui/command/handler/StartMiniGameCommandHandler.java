package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.StartMiniGameCommand;
import coffeeshout.room.application.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartMiniGameCommandHandler implements MiniGameCommandHandler<StartMiniGameCommand> {

    private final RoomService roomService;

    @Autowired
    public StartMiniGameCommandHandler(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void handle(String joinCode, StartMiniGameCommand command) {
        roomService.startNextGame(joinCode, command.hostName());
    }

    @Override
    public Class<StartMiniGameCommand> getCommandType() {
        return StartMiniGameCommand.class;
    }
}
