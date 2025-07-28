package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.StartCardGameCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCardGameCommandHandler implements MiniGameCommandHandler<StartCardGameCommand> {

    private final CardGameService cardGameService;

    @Override
    public void handle(String joinCode, StartCardGameCommand command) {
        cardGameService.startGame(joinCode);
    }

    @Override
    public Class<StartCardGameCommand> getCommandType() {
        return null;
    }
}
