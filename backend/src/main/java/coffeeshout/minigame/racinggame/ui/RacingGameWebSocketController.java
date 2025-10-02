package coffeeshout.minigame.racinggame.ui;

import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.racinggame.application.RacingGameService;
import coffeeshout.minigame.racinggame.domain.event.RacingGameStarted;
import coffeeshout.minigame.racinggame.domain.event.StartRacingGameCommandEvent;
import coffeeshout.minigame.racinggame.domain.event.TapCommandEvent;
import coffeeshout.minigame.racinggame.infra.messaging.RacingGameEventPublisher;
import coffeeshout.minigame.racinggame.ui.request.StartRacingGameCommand;
import coffeeshout.minigame.racinggame.ui.request.TapCommand;
import coffeeshout.room.ui.response.PlayerResponse;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import java.time.Instant;
import java.util.List;
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

    private final RacingGameService racingGameService;

    @MessageMapping("/room/{joinCode}/racing-game/start")
    @Operation(
            summary = "레이싱 게임 시작",
            description = "레이싱 게임을 시작하는 웹소켓 요청입니다. 호스트만 요청할 수 있습니다."
    )
    @MessageResponse(
            path = "/topic/room/%s/racing-game/state",
            returnType = RacingGameStarted.class
    )
    public void startGame(@DestinationVariable String joinCode, @Payload StartRacingGameCommand command) {
        racingGameService.startGame(joinCode, command.hostName());
    }

    @MessageMapping("/room/{joinCode}/racinggame/tap")
    @Operation(
            summary = "레이싱 게임 탭",
            description = "레이싱 게임에서 플레이어가 화면을 탭하는 웹소켓 요청입니다."
    )
    @MessageResponse(
            path = "/topic/room/%s/racing-game/state",
            returnType = RacingGameStarted.class
    )
    public void tap(@DestinationVariable String joinCode, @Payload TapCommand command) {
        racingGameService.tap(joinCode, command.playerName(), command.tapCount());
    }
}
