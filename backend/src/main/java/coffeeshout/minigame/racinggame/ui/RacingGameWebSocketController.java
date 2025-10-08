package coffeeshout.minigame.racinggame.ui;

import coffeeshout.minigame.racinggame.application.RacingGameService;
import coffeeshout.minigame.racinggame.domain.event.RaceStartedEvent;
import coffeeshout.minigame.racinggame.ui.request.TapCommand;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
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

    @MessageMapping("/room/{joinCode}/racing-game/tap")
    @Operation(
            summary = "레이싱 게임 탭",
            description = "레이싱 게임에서 플레이어가 화면을 탭하는 웹소켓 요청입니다."
    )
    @MessageResponse(
            path = "/topic/room/{joinCode}/racing-game/state",
            returnType = RaceStartedEvent.class
    )
    public void tap(@DestinationVariable String joinCode, @Payload TapCommand command) {
        racingGameService.tap(joinCode, command.playerName(), command.tapCount());
    }
}
