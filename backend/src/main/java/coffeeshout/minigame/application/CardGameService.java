package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.event.SelectCardCommandEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.messaging.CardSelectStreamProducer;
import coffeeshout.minigame.infra.messaging.MiniGameEventPublisher;
import coffeeshout.minigame.infra.persistance.MiniGameEntity;
import coffeeshout.minigame.infra.persistance.MiniGameJpaRepository;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.persistance.PlayerEntity;
import coffeeshout.room.infra.persistance.PlayerJpaRepository;
import coffeeshout.room.infra.persistance.RoomEntity;
import coffeeshout.room.infra.persistance.RoomJpaRepository;
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
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final MiniGameJpaRepository miniGameJpaRepository;
    private final CardSelectStreamProducer cardSelectStreamProducer;

    @Override
    public void publishStartEvent(String joinCode, String hostName) {
        final StartMiniGameCommandEvent event = new StartMiniGameCommandEvent(joinCode, hostName);
        miniGameEventPublisher.publishEvent(event);
        log.info("미니게임 시작 이벤트 발행: joinCode={}, hostName={}, eventId={}",
                joinCode, hostName, event.eventId());
    }

    @Override
    public void publishSelectCardEvent(String joinCode, String playerName, Integer cardIndex) {
        final SelectCardCommandEvent event = new SelectCardCommandEvent(joinCode, playerName, cardIndex);
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

    public void saveGameEntities(String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);

        // RoomEntity 찾아서 PlayerEntity들 저장
        final RoomEntity roomEntity = getRoomEntity(joinCode);
        roomEntity.updateRoomStatus(RoomState.PLAYING);

        final MiniGameEntity miniGameEntity = new MiniGameEntity(roomEntity, MiniGameType.CARD_GAME);
        miniGameJpaRepository.save(miniGameEntity);

        room.getPlayers().forEach(player -> {
            final PlayerEntity playerEntity = new PlayerEntity(
                    roomEntity,
                    player.getName().value(),
                    player.getPlayerType()
            );
            playerJpaRepository.save(playerEntity);
        });
    }

    public void selectCardInternal(String joinCode, String playerName, Integer cardIndex) {
        cardGameCommandService.selectCard(new JoinCode(joinCode), new PlayerName(playerName), cardIndex);
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }

    private RoomEntity getRoomEntity(String joinCode) {
        return roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다: " + joinCode));
    }
}
