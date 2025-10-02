package coffeeshout.minigame.racinggame.domain;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.racinggame.domain.event.RacingGameStarted;
import coffeeshout.minigame.racinggame.domain.event.RunnersMoved;
import generator.annotaions.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/*
    TODO: redis도입시 event발송에서 redis pub/sub message 발송으로 변경하기
 */

@Component
@RequiredArgsConstructor
public class RacingGameMessagePublisher {

    private static final String RACING_GAME_PLAYERS_POSITION_DESTINATION_FORMAT = "/topic/room/%s/racing-game";
    private static final String RACING_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/racing-game/state";
    private final LoggingSimpMessagingTemplate loggingSimpMessagingTemplate;

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game",
            returnType = WebSocketResponse.class,
            genericType = RunnersMoved.class
    )
    public void publishRunnersPosition(RunnersMoved runnersMoved) {
        loggingSimpMessagingTemplate.convertAndSend(
                RACING_GAME_PLAYERS_POSITION_DESTINATION_FORMAT,
                WebSocketResponse.success(runnersMoved)
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game/state",
            returnType = WebSocketResponse.class,
            genericType = RacingGameStarted.class
    )
    public void publishRacingGameStart(RacingGameStarted racingGameStarted) {
        loggingSimpMessagingTemplate.convertAndSend(
                RACING_GAME_STATE_DESTINATION_FORMAT,
                WebSocketResponse.success(racingGameStarted)
        );
    }
}
