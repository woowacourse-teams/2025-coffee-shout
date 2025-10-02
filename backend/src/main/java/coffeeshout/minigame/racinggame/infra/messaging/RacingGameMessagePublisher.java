package coffeeshout.minigame.racinggame.infra.messaging;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.racinggame.domain.RacingGameState;
import coffeeshout.minigame.racinggame.domain.dto.RacingGameRunnersStateResponse;
import coffeeshout.minigame.racinggame.domain.dto.RacingGameStateResponse;
import coffeeshout.minigame.racinggame.domain.event.RaceFinished;
import coffeeshout.minigame.racinggame.domain.event.RaceStarted;
import coffeeshout.minigame.racinggame.domain.event.RunnersMoved;
import generator.annotaions.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
            genericType = RacingGameRunnersStateResponse.class
    )
    public void publishRunnersPosition(RunnersMoved runnersMoved) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_PLAYERS_POSITION_DESTINATION_FORMAT, runnersMoved.joinCode()),
                WebSocketResponse.success(new RacingGameRunnersStateResponse(
                        runnersMoved.racingRange(), runnersMoved.runnerPositions()
                ))
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game/state",
            returnType = WebSocketResponse.class,
            genericType = RacingGameState.class
    )
    public void publishRacingGameStart(RaceStarted raceStarted) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_STATE_DESTINATION_FORMAT, raceStarted.joinCode()),
                WebSocketResponse.success(new RacingGameStateResponse(raceStarted.state()))
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game/state",
            returnType = WebSocketResponse.class,
            genericType = RacingGameState.class
    )
    public void publishRacingGameFinish(RaceFinished raceFinished) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_STATE_DESTINATION_FORMAT, raceFinished.joinCode()),
                WebSocketResponse.success(new RacingGameStateResponse(raceFinished.state()))
        );
    }
}
