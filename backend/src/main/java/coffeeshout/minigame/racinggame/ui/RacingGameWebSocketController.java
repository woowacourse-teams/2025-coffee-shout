package coffeeshout.minigame.racinggame.ui;

import coffeeshout.minigame.racinggame.domain.event.StartRacingGameCommandEvent;
import coffeeshout.minigame.racinggame.domain.event.TapCommandEvent;
import coffeeshout.minigame.racinggame.infra.messaging.RacingGameEventPublisher;
import coffeeshout.minigame.racinggame.ui.request.StartRacingGameCommand;
import coffeeshout.minigame.racinggame.ui.request.TapCommand;
import generator.annotaions.Operation;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RacingGameWebSocketController {

    private final RacingGameEventPublisher racingGameEventPublisher;

    @MessageMapping("/room/{joinCode}/racinggame/start")
    @Operation(
            summary = "레이싱 게임 시작",
            description = "레이싱 게임을 시작하는 웹소켓 요청입니다. 호스트만 요청할 수 있습니다."
    )
    public void startGame(@DestinationVariable String joinCode, @Payload StartRacingGameCommand command) {
        final StartRacingGameCommandEvent event = StartRacingGameCommandEvent.create(joinCode, command.hostName());
        racingGameEventPublisher.publishEvent(event);
        log.info("레이싱 게임 시작 이벤트 발행: joinCode={}, hostName={}, eventId={}",
                joinCode, command.hostName(), event.eventId());
    }

    @MessageMapping("/room/{joinCode}/racinggame/tap")
    @Operation(
            summary = "레이싱 게임 탭",
            description = "레이싱 게임에서 플레이어가 화면을 탭하는 웹소켓 요청입니다."
    )
    public void tap(@DestinationVariable String joinCode, @Payload TapCommand command) {
        final TapCommandEvent event = TapCommandEvent.create(joinCode, command.playerName(), command.tapCount(), Instant.now());
        racingGameEventPublisher.publishEvent(event);
        log.debug("탭 이벤트 발행: joinCode={}, playerName={}, tapCount={}, eventId={}",
                joinCode, command.playerName(), command.tapCount(), event.eventId());
    }
}
