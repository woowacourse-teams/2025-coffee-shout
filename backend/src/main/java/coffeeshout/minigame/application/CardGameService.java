package coffeeshout.minigame.application;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.round.RoomRoundManager;
import coffeeshout.minigame.domain.cardgame.round.RoundManagerRegistry;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CardGameService implements MiniGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final RoomQueryService roomQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoundManagerRegistry roundManagerRegistry;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            SimpMessagingTemplate messagingTemplate,
            RoundManagerRegistry roundManagerRegistry
    ) {
        this.roomQueryService = roomQueryService;
        this.messagingTemplate = messagingTemplate;
        this.roundManagerRegistry = roundManagerRegistry;
    }

    @Override
    public void start(Playable playable, String joinCode) {
        sendGameStartMessage(joinCode, playable.getMiniGameType());
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.findByJoinCode(roomJoinCode);
        final CardGame cardGame = (CardGame) playable;

        log.info("방 {} - 카드게임 시작", joinCode);
        
        // 해당 방의 전용 RoundManager 가져오기 (없으면 새로 생성)
        RoomRoundManager roomRoundManager = roundManagerRegistry.getOrCreate(roomJoinCode);
        
        cardGame.startGame(room.getPlayers());
        roomRoundManager.executePhase(cardGame, room,
                () -> sendCardGameState(roomJoinCode));
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));

        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomJoinCode);

        if (cardGame.allPlayersSelected()) {
            log.info("방 {} - 모든 플레이어 카드 선택 완료, 조기 전환 트리거", joinCode);
            
            // 해당 방의 RoundManager 가져오기
            RoomRoundManager roomRoundManager = roundManagerRegistry.get(roomJoinCode);
            if (roomRoundManager != null && roomRoundManager.isActive()) {
                Room room = roomQueryService.findByJoinCode(roomJoinCode);
                roomRoundManager.triggerEarlyTransition(cardGame, room,
                        () -> sendCardGameState(roomJoinCode));
            } else {
                log.warn("방 {} - RoundManager를 찾을 수 없거나 비활성 상태", joinCode);
            }
        }
    }

    private void sendCardGameState(JoinCode joinCode) {
        final CardGame cardGame = getCardGame(joinCode);
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.value());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }

    private void sendGameStartMessage(String joinCode, MiniGameType miniGameType) {
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode),
                WebSocketResponse.success(new MiniGameStartMessage(miniGameType))
        );
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
    
    /**
     * 방이 종료될 때 해당 방의 RoundManager를 정리합니다.
     */
    public void cleanupRoom(String joinCode) {
        JoinCode roomJoinCode = new JoinCode(joinCode);
        log.info("방 {} 정리 요청", joinCode);
        roundManagerRegistry.remove(roomJoinCode);
    }
    
    /**
     * 현재 활성화된 방의 수를 반환합니다. (모니터링용)
     */
    public int getActiveRoomCount() {
        return roundManagerRegistry.getActiveRoomCount();
    }
}
