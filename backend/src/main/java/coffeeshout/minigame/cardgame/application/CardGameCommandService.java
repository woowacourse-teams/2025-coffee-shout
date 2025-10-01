package coffeeshout.minigame.cardgame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.cardgame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 카드 게임의 비즈니스 로직을 처리하는 Command Service
 * 이벤트 핸들러에서 호출되며, 실제 게임 로직을 실행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniGameCommandService implements MiniGameService {

    private final RoomQueryService roomQueryService;
    private final CardGameCommandService cardGameCommandService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 카드 게임을 시작합니다.
     * 1. Room 상태를 PLAYING으로 변경
     * 2. CardGameStartedEvent 발행 (Spring ApplicationEvent)
     */
    public void startGame(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);

        // Room 상태 변경
        room.startNextGame(hostName);

        // 카드 게임 시작 이벤트 발행
        final CardGame cardGame = getCardGame(room);
        eventPublisher.publishEvent(new CardGameStartedEvent(roomJoinCode, cardGame));

        log.info("카드 게임 시작 완료: joinCode={}", joinCode);
    }

    /**
     * 플레이어의 카드 선택을 처리합니다.
     */
    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        cardGameCommandService.selectCard(
                new JoinCode(joinCode),
                new PlayerName(playerName),
                cardIndex
        );
        log.debug("카드 선택 완료: joinCode={}, playerName={}, cardIndex={}", joinCode, playerName, cardIndex);
    }

    private CardGame getCardGame(Room room) {
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
