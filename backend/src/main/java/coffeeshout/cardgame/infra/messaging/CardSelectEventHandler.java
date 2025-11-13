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

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectEventHandler implements StreamEventHandler<SelectCardCommandEvent> {

    private final CardGameCommandService cardGameCommandService;

    @Override
    public void handle(SelectCardCommandEvent event) {
        log.info("카드 선택 이벤트 처리 시작: eventId={}, joinCode={}, playerName={}, cardIndex={}",
                event.eventId(), event.joinCode(), event.playerName(), event.cardIndex());

        try {
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
