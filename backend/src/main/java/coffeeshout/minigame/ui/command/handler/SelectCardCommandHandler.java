package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import org.springframework.stereotype.Component;

@Component
public class SelectCardCommandHandler implements MiniGameCommandHandler<SelectCardCommand> {

    private final CardGameService cardGameService;

    public SelectCardCommandHandler(CardGameService cardGameService) {
        this.cardGameService = cardGameService;
    }

    @Override
    public void handle(String joinCode, SelectCardCommand command) {
        cardGameService.selectCard(joinCode, command.playerName(), command.cardIndex());
    }

    @Override
    public Class<SelectCardCommand> getCommandType() {
        return SelectCardCommand.class;
    }
}
