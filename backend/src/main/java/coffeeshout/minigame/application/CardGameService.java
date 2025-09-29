package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.domain.event.SelectCardCommandEvent;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.MiniGameEventPublisher;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.persistance.PlayerEntity;
import coffeeshout.room.infra.persistance.PlayerJpaRepository;
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
    private final ApplicationEventPublisher eventPublisher;
    private final MiniGameEventPublisher miniGameEventPublisher;
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;

    @Override
    public void publishStartEvent(String joinCode, String hostName) {
        final StartMiniGameCommandEvent event = StartMiniGameCommandEvent.create(joinCode, hostName);
        miniGameEventPublisher.publishEvent(event);
        log.info("미니게임 시작 이벤트 발행: joinCode={}, hostName={}, eventId={}",
                joinCode, hostName, event.getEventId());
    }

    @Override
    public void publishSelectCardEvent(String joinCode, String playerName, Integer cardIndex) {
        final SelectCardCommandEvent event = SelectCardCommandEvent.create(joinCode, playerName, cardIndex);
        miniGameEventPublisher.publishEvent(event);
        log.info("카드 선택 이벤트 발행: joinCode={}, playerName={}, cardIndex={}, eventId={}",
                joinCode, playerName, cardIndex, event.getEventId());
    }

    // === Internal 메서드들 (Redis 리스너용) ===

    public void startInternal(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);

        updateRoomEntity(joinCode, room);

        eventPublisher.publishEvent(new CardGameStartedEvent(roomJoinCode, cardGame));
    }

    public void selectCardInternal(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        eventPublisher.publishEvent(new CardSelectedEvent(roomJoinCode, cardGame));
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }

    private void updateRoomEntity(String joinCode, Room room) {
        // RoomEntity 찾아서 PlayerEntity들 저장
        roomJpaRepository.findByJoinCode(joinCode).ifPresent(roomEntity -> {
            roomEntity.updateRoomStatus(RoomState.PLAYING);
            room.getPlayers().forEach(player -> {
                final PlayerEntity playerEntity = new PlayerEntity(
                        roomEntity,
                        player.getName().value(),
                        player.getPlayerType()
                );
                playerJpaRepository.save(playerEntity);
            });
        });
    }
}
