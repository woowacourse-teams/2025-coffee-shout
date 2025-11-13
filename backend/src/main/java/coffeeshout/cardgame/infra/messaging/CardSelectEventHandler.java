package coffeeshout.cardgame.infra.messaging;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.cardgame.domain.service.CardGameCommandService;
import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.global.infra.messaging.StreamEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.player.PlayerName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 카드 선택 이벤트를 처리하는 Handler
 * <p>
 * {@link SelectCardCommandEvent}를 처리하여 플레이어의 카드 선택을 게임에 반영하는 비즈니스 로직을 담당합니다.
 * 메시징 인프라로부터 분리되어 독립적으로 테스트 가능하며, 재사용 가능합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *   <li>플레이어의 카드 선택 정보를 도메인 객체로 변환</li>
 *   <li>카드 게임 서비스를 통해 카드 선택 처리</li>
 *   <li>비즈니스 예외 처리 및 로깅</li>
 * </ul>
 *
 * @see SelectCardCommandEvent
 * @see StreamEventHandler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectEventHandler implements StreamEventHandler<SelectCardCommandEvent> {

    private final CardGameCommandService cardGameCommandService;

    /**
     * 카드 선택 이벤트를 처리합니다.
     * <p>
     * 플레이어가 선택한 카드를 게임에 반영합니다.
     * 이 이벤트는 fire-and-forget 방식으로 처리되며, 별도의 응답을 반환하지 않습니다.
     * </p>
     *
     * @param event 카드 선택 명령 이벤트
     * @throws InvalidArgumentException 잘못된 입력값 (존재하지 않는 joinCode, playerName, 잘못된 cardIndex 등)
     * @throws InvalidStateException    잘못된 상태 (게임이 시작되지 않음, 이미 카드를 선택함 등)
     */
    @Override
    public void handle(SelectCardCommandEvent event) {
        log.info("카드 선택 이벤트 처리 시작: eventId={}, joinCode={}, playerName={}, cardIndex={}",
                event.eventId(), event.joinCode(), event.playerName(), event.cardIndex());

        try {
            // 도메인 객체로 변환하여 카드 선택 처리
            final JoinCode joinCode = new JoinCode(event.joinCode());
            final PlayerName playerName = new PlayerName(event.playerName());

            cardGameCommandService.selectCard(joinCode, playerName, event.cardIndex());

            log.info("카드 선택 처리 성공: joinCode={}, playerName={}, cardIndex={}, eventId={}",
                    event.joinCode(), event.playerName(), event.cardIndex(), event.eventId());

        } catch (InvalidArgumentException | InvalidStateException e) {
            log.warn("카드 선택 처리 중 비즈니스 오류: joinCode={}, playerName={}, cardIndex={}, eventId={}, error={}",
                    event.joinCode(), event.playerName(), event.cardIndex(), event.eventId(), e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("카드 선택 처리 중 시스템 오류: joinCode={}, playerName={}, cardIndex={}, eventId={}",
                    event.joinCode(), event.playerName(), event.cardIndex(), event.eventId(), e);
            throw e;
        }
    }
}
