package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.domain.event.SelectCardCommandEvent;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.MiniGameEventPublisher;
import coffeeshout.minigame.infra.MiniGameEventWaitManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private final MiniGameEventWaitManager miniGameEventWaitManager;

    // === 비동기 메서드들 (WebSocket Controller용) ===

    @Override
    public CompletableFuture<Void> startAsync(Playable playable, String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player host = room.getHost();
        final StartMiniGameCommandEvent event = StartMiniGameCommandEvent.create(joinCode, host.getName().value());

        final CompletableFuture<Void> future = miniGameEventWaitManager.registerWait(event.getEventId());
        miniGameEventPublisher.publishEvent(event);

        return future.orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    miniGameEventWaitManager.cleanup(event.getEventId());
                    if (throwable != null) {
                        log.error("미니게임 시작 비동기 처리 실패: eventId={}, joinCode={}",
                                event.getEventId(), joinCode, throwable);
                        return;
                    }
                    log.info("미니게임 시작 비동기 처리 완료: joinCode={}, eventId={}",
                            joinCode, event.getEventId());
                });
    }

    public CompletableFuture<Void> selectCardAsync(String joinCode, String playerName, Integer cardIndex) {
        final SelectCardCommandEvent event = SelectCardCommandEvent.create(joinCode, playerName, cardIndex);

        final CompletableFuture<Void> future = miniGameEventWaitManager.registerWait(event.getEventId());
        miniGameEventPublisher.publishEvent(event);

        return future.orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    miniGameEventWaitManager.cleanup(event.getEventId());
                    if (throwable != null) {
                        log.error("카드 선택 비동기 처리 실패: eventId={}, joinCode={}, playerName={}, cardIndex={}",
                                event.getEventId(), joinCode, playerName, cardIndex, throwable);
                        return;
                    }
                    log.info("카드 선택 비동기 처리 완료: joinCode={}, playerName={}, eventId={}",
                            joinCode, playerName, event.getEventId());
                });
    }

    // === 기존 동기 메서드들 (테스트용 + 하위 호환성) ===

    @Override
    public void start(Playable playable, String joinCode) {
        try {
            startAsync(playable, joinCode).join();
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("미니게임 시작 실패", cause);
        }
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        try {
            selectCardAsync(joinCode, playerName, cardIndex).join();
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("카드 선택 실패", cause);
        }
    }

    // === Internal 메서드들 (Redis 리스너용) ===

    public void startInternal(String joinCode, String hostName) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
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
}
