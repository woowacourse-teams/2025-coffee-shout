package coffeeshout.minigame.cardgame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStartMessage;
import coffeeshout.minigame.cardgame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.cardgame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.cardgame.infra.persistence.MiniGameJpaRepository;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
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
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final MiniGameJpaRepository miniGameJpaRepository;

    @Override
    public void start(String joinCode, String hostName) {
        eventPublisher.publishEvent(new CardGameStartMessage(
                joinCode,
                CardGameTaskType.getFirstTask().name()
        ));
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

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
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
