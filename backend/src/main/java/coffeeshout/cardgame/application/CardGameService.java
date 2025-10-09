package coffeeshout.cardgame.application;

import coffeeshout.global.metric.GameDurationMetricService;
import coffeeshout.minigame.domain.MiniGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.cardgame.domain.CardGame;
import coffeeshout.cardgame.domain.CardGameTaskType;
import coffeeshout.cardgame.domain.event.dto.CardGameStartMessage;
import coffeeshout.cardgame.domain.service.CardGameCommandService;
import coffeeshout.minigame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.infra.persistence.MiniGameJpaRepository;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CardGameService implements MiniGameService {

    private final RoomQueryService roomQueryService;
    private final CardGameCommandService cardGameCommandService;
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final MiniGameJpaRepository miniGameJpaRepository;
    private final TaskScheduler taskScheduler;
    private final GameDurationMetricService gameDurationMetricService;
    private final ApplicationEventPublisher eventPublisher;

    public CardGameService(
            RoomQueryService roomQueryService,
            CardGameCommandService cardGameCommandService,
            RoomJpaRepository roomJpaRepository,
            PlayerJpaRepository playerJpaRepository,
            MiniGameJpaRepository miniGameJpaRepository,
            @Qualifier("miniGameTaskScheduler") TaskScheduler taskScheduler,
            GameDurationMetricService gameDurationMetricService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.roomQueryService = roomQueryService;
        this.cardGameCommandService = cardGameCommandService;
        this.roomJpaRepository = roomJpaRepository;
        this.playerJpaRepository = playerJpaRepository;
        this.miniGameJpaRepository = miniGameJpaRepository;
        this.taskScheduler = taskScheduler;
        this.gameDurationMetricService = gameDurationMetricService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start(String joinCode, String hostName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final CardGame cardGame = getCardGame(room);
        CardGameTaskType.getFirstTask().processTask(cardGame, room, taskScheduler, eventPublisher);
        gameDurationMetricService.startGameTimer(joinCode);
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

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        cardGameCommandService.selectCard(new JoinCode(joinCode), new PlayerName(playerName), cardIndex);
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }

    private RoomEntity getRoomEntity(String joinCode) {
        return roomJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다: " + joinCode));
    }

    private CardGame getCardGame(Room room) {
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
