package coffeeshout.minigame.ui.command.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SelectCardCommandHandler implements MiniGameCommandHandler<SelectCardCommand> {

    private final CardGameService cardGameService;

    @Override
    public void handle(String joinCode, SelectCardCommand command) {
        // 비동기 처리로 변경
        cardGameService.selectCardAsync(joinCode, command.playerName(), command.cardIndex());
    }

    @Override
    public Class<SelectCardCommand> getCommandType() {
        return SelectCardCommand.class;
    }
}
