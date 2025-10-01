package coffeeshout.minigame.cardgame.ui.command.handler;

import coffeeshout.minigame.cardgame.application.CardGameService;
import coffeeshout.minigame.cardgame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.cardgame.ui.request.command.SelectCardCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SelectCardCommandHandler implements MiniGameCommandHandler<SelectCardCommand> {

    private final CardGameService cardGameService;

    @Override
    public void handle(String joinCode, SelectCardCommand command) {
        // 바로 이벤트 발행만 함 - 모든 인스턴스가 동시에 처리하게 함
        cardGameService.publishSelectCardEvent(joinCode, command.playerName(), command.cardIndex());
    }

    @Override
    public Class<SelectCardCommand> getCommandType() {
        return SelectCardCommand.class;
    }
}
