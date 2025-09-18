package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CardGameService implements MiniGameService {

    private final RoomQueryService roomQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.roomQueryService = roomQueryService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start(Playable playable, String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = (CardGame) playable;
        eventPublisher.publishEvent(new CardGameStartEvent(roomJoinCode, cardGame));
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        eventPublisher.publishEvent(new CardSelectEvent(roomJoinCode, cardGame));
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
