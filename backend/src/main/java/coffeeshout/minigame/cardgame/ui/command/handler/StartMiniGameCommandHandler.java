package coffeeshout.minigame.cardgame.ui.command.handler;

import coffeeshout.minigame.cardgame.application.CardGameService;
import coffeeshout.minigame.cardgame.application.MiniGameService;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.ui.command.MiniGameCommandHandler;
import coffeeshout.minigame.cardgame.ui.request.command.StartMiniGameCommand;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartMiniGameCommandHandler implements MiniGameCommandHandler<StartMiniGameCommand> {

    private final Map<MiniGameType, MiniGameService> services;

    @Autowired
    public StartMiniGameCommandHandler(CardGameService cardGameService) {
        services = new EnumMap<>(MiniGameType.class);
        services.put(MiniGameType.CARD_GAME, cardGameService);
    }

    @Override
    public void handle(String joinCode, StartMiniGameCommand command) {
        // 바로 이벤트 발행만 함 - 모든 인스턴스가 동시에 처리하게 함
        services.get(MiniGameType.CARD_GAME).publishStartEvent(joinCode, command.hostName());
    }

    @Override
    public Class<StartMiniGameCommand> getCommandType() {
        return StartMiniGameCommand.class;
    }
}
