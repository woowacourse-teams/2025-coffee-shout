package coffeeshout.room.ui.messaging;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.event.broadcast.MiniGameListChangedEvent;
import coffeeshout.room.domain.event.broadcast.PlayerListChangedEvent;
import coffeeshout.room.domain.event.broadcast.QrCodeStatusChangedEvent;
import coffeeshout.room.domain.event.broadcast.RouletteShownEvent;
import coffeeshout.room.domain.event.broadcast.RouletteWinnerSelectedEvent;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.QrCodeStatusResponse;
import coffeeshout.room.ui.response.RoomStatusResponse;
import coffeeshout.room.ui.response.WinnerResponse;
import generator.annotaions.MessageResponse;
import generator.annotaions.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Room 도메인의 상태 변경을 WebSocket을 통해 브로드캐스트하는 Message Publisher
 * <p>
 * Spring Domain Event를 구독하여 WebSocket 메시지를 전송함으로써
 * Handler와 브로드캐스트 로직을 분리하고 단일 책임 원칙을 준수합니다.
 * </p>
 * <p>
 * 이 클래스는 Presentation Layer (UI)에 위치하며, 사용자에게 보여지는 WebSocket 응답을 관리합니다.
 * Response 객체 생성과 API 문서화를 함께 담당하여 일관성을 유지합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomMessagePublisher {

    private static final String PLAYER_LIST_TOPIC_FORMAT = "/topic/room/%s";
    private static final String MINI_GAME_TOPIC_FORMAT = "/topic/room/%s/minigame";
    private static final String ROULETTE_TOPIC_FORMAT = "/topic/room/%s/roulette";
    private static final String WINNER_TOPIC_FORMAT = "/topic/room/%s/winner";
    private static final String QR_CODE_TOPIC_FORMAT = "/topic/room/%s/qr-code";

    private final LoggingSimpMessagingTemplate messagingTemplate;

    /**
     * 플레이어 목록 변경 이벤트를 수신하여 브로드캐스트
     */
    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}",
            returnType = List.class,
            genericType = PlayerResponse.class
    )
    @Operation(
            summary = "플레이어 목록 브로드캐스트",
            description = "변경된 플레이어 목록을 방의 모든 참가자에게 전송합니다."
    )
    public void onPlayerListChanged(PlayerListChangedEvent event) {
        log.debug("플레이어 목록 변경 이벤트 수신: joinCode={}, playerCount={}",
                event.joinCode(), event.players().size());

        final List<PlayerResponse> responses = event.players().stream()
                .map(PlayerResponse::from)
                .toList();

        final String destination = String.format(PLAYER_LIST_TOPIC_FORMAT, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(responses));

        log.debug("플레이어 목록 브로드캐스트 완료: joinCode={}", event.joinCode());
    }

    /**
     * 미니게임 목록 변경 이벤트를 수신하여 브로드캐스트
     */
    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/minigame",
            returnType = List.class,
            genericType = String.class
    )
    @Operation(
            summary = "미니게임 목록 브로드캐스트",
            description = "선택된 미니게임 목록을 방의 모든 참가자에게 전송합니다."
    )
    public void onMiniGameListChanged(MiniGameListChangedEvent event) {
        log.debug("미니게임 목록 변경 이벤트 수신: joinCode={}, gameCount={}",
                event.joinCode(), event.miniGameTypes().size());

        final String destination = String.format(MINI_GAME_TOPIC_FORMAT, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(event.miniGameTypes()));

        log.debug("미니게임 목록 브로드캐스트 완료: joinCode={}", event.joinCode());
    }

    /**
     * 룰렛 화면 표시 이벤트를 수신하여 브로드캐스트
     */
    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/roulette",
            returnType = RoomStatusResponse.class
    )
    @Operation(
            summary = "룰렛 화면 전환 브로드캐스트",
            description = "룰렛 화면으로의 전환을 방의 모든 참가자에게 전송합니다."
    )
    public void onRouletteShown(RouletteShownEvent event) {
        log.debug("룰렛 화면 표시 이벤트 수신: joinCode={}, roomState={}",
                event.joinCode(), event.roomState());

        final RoomStatusResponse response = RoomStatusResponse.of(new JoinCode(event.joinCode()), event.roomState());
        final String destination = String.format(ROULETTE_TOPIC_FORMAT, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("룰렛 화면 전환 브로드캐스트 완료: joinCode={}", event.joinCode());
    }

    /**
     * 룰렛 당첨자 선택 이벤트를 수신하여 브로드캐스트
     */
    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/winner",
            returnType = WinnerResponse.class
    )
    @Operation(
            summary = "룰렛 당첨자 브로드캐스트",
            description = "룰렛으로 선택된 당첨자를 방의 모든 참가자에게 전송합니다."
    )
    public void onRouletteWinnerSelected(RouletteWinnerSelectedEvent event) {
        log.debug("룰렛 당첨자 선택 이벤트 수신: joinCode={}, winner={}",
                event.joinCode(), event.winner().name().value());

        final WinnerResponse response = WinnerResponse.from(event.winner());
        final String destination = String.format(WINNER_TOPIC_FORMAT, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("룰렛 당첨자 브로드캐스트 완료: joinCode={}, winner={}",
                event.joinCode(), event.winner().name().value());
    }

    /**
     * QR 코드 상태 변경 이벤트를 수신하여 브로드캐스트
     */
    @EventListener
    @MessageResponse(
            path = "/room/{joinCode}/qr-code",
            returnType = QrCodeStatusResponse.class
    )
    @Operation(
            summary = "QR 코드 상태 브로드캐스트",
            description = "QR 코드 생성 상태를 방의 모든 참가자에게 전송합니다."
    )
    public void onQrCodeStatusChanged(QrCodeStatusChangedEvent event) {
        log.debug("QR 코드 상태 변경 이벤트 수신: joinCode={}, status={}",
                event.joinCode(), event.status());

        final QrCodeStatusResponse response = new QrCodeStatusResponse(event.status(), event.qrCodeUrl());
        final String destination = String.format(QR_CODE_TOPIC_FORMAT, event.joinCode());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(response));

        log.debug("QR 코드 상태 브로드캐스트 완료: joinCode={}, status={}", event.joinCode(), event.status());
    }
}
