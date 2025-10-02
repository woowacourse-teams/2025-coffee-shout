package coffeeshout.minigame.cardgame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.event.SelectCardCommandEvent;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.cardgame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.cardgame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.cardgame.infra.messaging.CardSelectStreamProducer;
import coffeeshout.minigame.cardgame.infra.messaging.MiniGameEventPublisher;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardGameService implements MiniGameService {

    private final RoomQueryService roomQueryService;
    private final CardGameCommandService cardGameCommandService;
    private final ApplicationEventPublisher eventPublisher;
    private final MiniGameEventPublisher miniGameEventPublisher;
    private final CardSelectStreamProducer cardSelectStreamProducer;

    @Override
    public void publishStartEvent(String joinCode, String hostName) {
        final StartMiniGameCommandEvent event = StartMiniGameCommandEvent.create(joinCode, hostName);
        miniGameEventPublisher.publishEvent(event);
        log.info("미니게임 시작 이벤트 발행: joinCode={}, hostName={}, eventId={}",
                joinCode, hostName, event.eventId());
    }

    @Override
    public void publishSelectCardEvent(String joinCode, String playerName, Integer cardIndex) {
        final SelectCardCommandEvent event = SelectCardCommandEvent.create(joinCode, playerName, cardIndex);
        cardSelectStreamProducer.broadcastCardSelect(event);
        log.info("카드 선택 이벤트 발행: joinCode={}, playerName={}, cardIndex={}, eventId={}",
                joinCode, playerName, cardIndex, event.eventId());
    }

    // === Internal 메서드들 (Redis 리스너용) ===

    public void startInternal(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        eventPublisher.publishEvent(new CardGameStartedEvent(roomJoinCode, cardGame));
    }

    public void selectCardInternal(String joinCode, String playerName, Integer cardIndex) {
        cardGameCommandService.selectCard(new JoinCode(joinCode), new PlayerName(playerName), cardIndex);
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
