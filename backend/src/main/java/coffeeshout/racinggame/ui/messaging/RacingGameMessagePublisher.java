package coffeeshout.racinggame.ui.messaging;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.racinggame.domain.RacingGameState;
import coffeeshout.racinggame.domain.event.RaceFinishedEvent;
import coffeeshout.racinggame.domain.event.RaceStateChangedEvent;
import coffeeshout.racinggame.domain.event.RunnersMovedEvent;
import coffeeshout.racinggame.ui.response.RacingGameRunnersStateResponse;
import coffeeshout.racinggame.ui.response.RacingGameStateResponse;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * RacingGame 도메인의 상태 변경을 WebSocket을 통해 브로드캐스트하는 Message Publisher
 * <p>
 * Spring Domain Event를 구독하여 WebSocket 메시지를 전송함으로써
 * 비즈니스 로직과 브로드캐스트 로직을 분리합니다.
 * </p>
 * <p>
 * 이 클래스는 Presentation Layer (UI)에 위치하며, 사용자에게 보여지는 WebSocket 응답을 관리합니다.
 * Response 객체 생성과 API 문서화를 함께 담당하여 일관성을 유지합니다.
 * </p>
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
            returnType = RacingGameRunnersStateResponse.class
    )
    @Operation(
            summary = "레이싱게임 러너 위치 브로드캐스트",
            description = "레이싱게임에서 러너들의 현재 위치를 방의 모든 참가자에게 전송합니다."
    )
    public void publishRunnersPosition(RunnersMovedEvent runnersMovedEvent) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_PLAYERS_POSITION_DESTINATION_FORMAT, runnersMovedEvent.joinCode()),
                WebSocketResponse.success(new RacingGameRunnersStateResponse(
                        runnersMovedEvent.racingRange(), runnersMovedEvent.runnerPositions()
                ))
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game/state",
            returnType = RacingGameStateResponse.class
    )
    @Operation(
            summary = "레이싱게임 상태 변경 브로드캐스트",
            description = "레이싱게임의 상태(시작, 진행 중 등)를 방의 모든 참가자에게 전송합니다."
    )
    public void publishRacingGameStart(RaceStateChangedEvent raceStateChangedEvent) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_STATE_DESTINATION_FORMAT, raceStateChangedEvent.joinCode()),
                WebSocketResponse.success(new RacingGameStateResponse(raceStateChangedEvent.state()))
        );
    }

    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/racing-game/state",
            returnType = WebSocketResponse.class,
            genericType = RacingGameState.class
    )
    @Operation(
            summary = "레이싱게임 종료 브로드캐스트",
            description = "레이싱게임이 종료되었음을 방의 모든 참가자에게 전송합니다."
    )
    public void publishRacingGameFinish(RaceFinishedEvent raceFinishedEvent) {
        loggingSimpMessagingTemplate.convertAndSend(
                String.format(RACING_GAME_STATE_DESTINATION_FORMAT, raceFinishedEvent.joinCode()),
                WebSocketResponse.success(new RacingGameStateResponse(raceFinishedEvent.state()))
        );
    }
}
