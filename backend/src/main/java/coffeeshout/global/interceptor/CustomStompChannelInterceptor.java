package coffeeshout.global.interceptor;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.event.RoomStateUpdateEvent;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomStompChannelInterceptor implements ChannelInterceptor {

    private final WebSocketMetricService webSocketMetricService;
    private final RoomService roomService;
    private final RoomQueryService roomQueryService;
    private final MenuQueryService menuQueryService;
    private final ApplicationEventPublisher eventPublisher;

    // 중복 처리 방지용
    private final Set<String> processedDisconnections = ConcurrentHashMap.newKeySet();

    // 플레이어 세션 매핑 관리
    private final ConcurrentHashMap<String, String> playerSessionMap = new ConcurrentHashMap<>(); // "joinCode:playerName" -> sessionId
    private final ConcurrentHashMap<String, String> sessionPlayerMap = new ConcurrentHashMap<>(); // sessionId -> "joinCode:playerName"

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        final Object commandObj = accessor.getCommand();
        final String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof final StompCommand command)) {
            return message;
        }

        try {
            switch (command) {
                case CONNECT:
                    log.info("WebSocket 연결 시작: sessionId={}", sessionId);
                    final String joinCode = accessor.getFirstNativeHeader("joinCode");
                    final String playerName = accessor.getFirstNativeHeader("playerName");
                    final String menuId = accessor.getFirstNativeHeader("menuId");

                    if (joinCode != null && playerName != null && menuId != null) {
                        final String playerKey = joinCode + ":" + playerName;

                        // 기존 세션 있으면 재연결, 없으면 첫 연결
                        String oldSessionId = playerSessionMap.get(playerKey);
                        if (oldSessionId != null) {
                            log.info("기존 플레이어 세션 정리: playerKey={}, oldSessionId={}", playerKey, oldSessionId);
                            sessionPlayerMap.remove(oldSessionId);
                            playerSessionMap.remove(playerKey);

                            playerSessionMap.put(playerKey, sessionId);
                            sessionPlayerMap.put(sessionId, playerKey);
                            log.info("플레이어 재연결 매핑: playerKey={}, sessionId={}", playerKey, sessionId);

                            // 재연결 처리
                            handlePlayerReconnection(joinCode, playerName, Long.parseLong(menuId), sessionId);
                        } else {
                            playerSessionMap.put(playerKey, sessionId);
                            sessionPlayerMap.put(sessionId, playerKey);
                            log.info("플레이어 첫 연결 매핑: playerKey={}, sessionId={}", playerKey, sessionId);

                            // 첫 연결 처리  
                            handlePlayerFirstConnection(joinCode, playerName, sessionId);
                        }
                    }

                    webSocketMetricService.startConnection(sessionId);
                    break;

                case SUBSCRIBE:
                    log.debug("구독 요청: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case UNSUBSCRIBE:
                    log.debug("구독 해제: sessionId={}", sessionId);
                    break;

                case SEND:
                    log.debug("클라이언트 메시지: sessionId={}, destination={}", sessionId, accessor.getDestination());
                    break;

                case DISCONNECT:
                    log.info("WebSocket 연결 해제 요청: sessionId={}", sessionId);
                    final String disconnectedPlayerKey = sessionPlayerMap.get(sessionId);
                    if (disconnectedPlayerKey != null) {
                        log.info("플레이어 세션 해제: playerKey={}, sessionId={}", disconnectedPlayerKey, sessionId);

                        // 방에서 플레이어 제거
                        handlePlayerDisconnection(disconnectedPlayerKey, sessionId, "CLIENT_DISCONNECT");
                    }
                    // DISCONNECT는 postSend에서만 처리하도록 변경
                    break;

                case ERROR:
                    final String errorMessage = accessor.getMessage();
                    log.error("STOMP 에러: sessionId={}, message={}", sessionId, errorMessage);

                    // 에러 발생 시 플레이어 제거
                    final String errorPlayerKey = sessionPlayerMap.remove(sessionId);
                    if (errorPlayerKey != null) {
                        playerSessionMap.remove(errorPlayerKey);
                        handlePlayerDisconnection(errorPlayerKey, sessionId, "STOMP_ERROR");
                    }

                    webSocketMetricService.recordDisconnection(sessionId, "stomp_error", false);
                    break;

                default:
                    log.trace("기타 STOMP 명령: sessionId={}, command={}", sessionId, command);
                    break;
            }
        } catch (Exception e) {
            log.error("STOMP 인터셉터 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return;
        }

        final Object commandObj = accessor.getCommand();
        final String sessionId = accessor.getSessionId();

        // STOMP 명령이 아닌 내부 메시지는 무시
        if (!(commandObj instanceof final StompCommand command)) {
            return;
        }

        try {
            if (command == StompCommand.CONNECT && sent) {
                // 서버에서 CONNECTED 응답을 성공적으로 보냈을 때 연결 완료 처리
                log.info("WebSocket 연결 완료: sessionId={}", sessionId);
                webSocketMetricService.completeConnection(sessionId);
            } else if (command == StompCommand.DISCONNECT && sent) {
                // DISCONNECT 메시지 전송 완료 시 연결 해제 처리 (중복 방지)
                if (processedDisconnections.add(sessionId)) {
                    log.info("WebSocket 연결 해제 완료: sessionId={}", sessionId);
                    webSocketMetricService.recordDisconnection(sessionId, "client_disconnect", true);
                } else {
                    log.debug("중복 DISCONNECT 무시: sessionId={}", sessionId);
                }
            } else if (!sent) {
                // 메시지 전송 실패
                log.warn("STOMP 메시지 전송 실패: sessionId={}, command={}", sessionId, command);

                if (command == StompCommand.CONNECTED) {
                    // 연결 응답 실패 - 플레이어 제거
                    final String failedPlayerKey = sessionPlayerMap.remove(sessionId);
                    if (failedPlayerKey != null) {
                        playerSessionMap.remove(failedPlayerKey);
                        handlePlayerDisconnection(failedPlayerKey, sessionId, "CONNECTION_FAILED");
                    }
                    webSocketMetricService.failConnection(sessionId, "connection_response_failed");
                }
            }
        } catch (Exception e) {
            log.error("STOMP postSend 처리 중 에러: sessionId={}, command={}", sessionId, command, e);
        }
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return message;
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    private void handlePlayerFirstConnection(String joinCode, String playerName, String sessionId) {
        log.info("플레이어 첫 연결: joinCode={}, playerName={}", joinCode, playerName);
        // 첫 연결은 별도 검증 없이 성공 처리
        // 실제 방 참여는 REST API로 이미 되어있을 것
    }

    private void handlePlayerReconnection(String joinCode, String playerName, Long menuId, String newSessionId) {
        try {
            // 1. 방 존재 확인
            final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));

            // 2. 방에 플레이어 재생성
            final Menu menu = menuQueryService.findById(menuId);
            room.reJoin(new PlayerName(playerName), menu);

            // 3. 방 상태 확인
            if (room.isPlayingState()) {
                log.info("게임 중인 방 재연결 거부: joinCode={}, playerName={}", joinCode, playerName);
                disconnectSession(newSessionId, "GAME_IN_PROGRESS");
                return;
            }

            // 4. READY 상태면 재연결 허용 + 현재 상태 전송
            log.info("방 재연결 허용: joinCode={}, playerName={}", joinCode, playerName);
            sendCurrentRoomState(joinCode, newSessionId);

        } catch (Exception e) {
            log.warn("재연결 실패: joinCode={}, playerName={}, error={}", joinCode, playerName, e.getMessage());
            // 재연결 실패 시 기존 매핑 제거하고 방에서 플레이어 제거
            final String playerKey = joinCode + ":" + playerName;
            playerSessionMap.remove(playerKey);
            sessionPlayerMap.remove(newSessionId);
            handlePlayerDisconnection(playerKey, newSessionId, "RECONNECTION_FAILED");
        }
    }

    private void sendCurrentRoomState(String joinCode, String newSessionId) {
        try {
            log.info("방 상태 전송 이벤트 발행: joinCode={}, sessionId={}", joinCode, newSessionId);
            eventPublisher.publishEvent(new RoomStateUpdateEvent(joinCode, "PLAYER_RECONNECTED"));
        } catch (Exception e) {
            log.error("방 상태 전송 실패: joinCode={}, sessionId={}", joinCode, newSessionId, e);
        }
    }

    private void disconnectSession(String sessionId, String reason) {
        try {
            // 에러 메시지는 로그로만 남기고, 클라이언트는 연결 실패로 알아서 처리하게 함
            log.warn("세션 연결 거부: sessionId={}, reason={}", sessionId, reason);
        } catch (Exception e) {
            log.error("세션 거부 처리 실패: sessionId={}, reason={}", sessionId, reason, e);
        }
    }

    private void handlePlayerDisconnection(String playerKey, String sessionId, String reason) {
        try {
            final String[] parts = playerKey.split(":");
            if (parts.length != 2) {
                log.warn("잘못된 플레이어 키 형식: {}", playerKey);
                return;
            }

            final String joinCode = parts[0];
            final String playerName = parts[1];

            log.info("플레이어 연결 해제 처리: joinCode={}, playerName={}, reason={}", joinCode, playerName, reason);

            // 방에서 플레이어 제거
            removePlayerFromRoom(joinCode, playerName);

        } catch (Exception e) {
            log.error("플레이어 연결 해제 처리 실패: playerKey={}, sessionId={}, reason={}", playerKey, sessionId, reason, e);
        }
    }

    private void removePlayerFromRoom(String joinCode, String playerName) {
        try {
            // 방에서 플레이어 제거
            boolean removed = roomService.removePlayer(joinCode, playerName);

            if (removed) {
                log.info("플레이어 방에서 제거 완료: joinCode={}, playerName={}", joinCode, playerName);

                // 이벤트 발행으로 브로드캐스트
                eventPublisher.publishEvent(new RoomStateUpdateEvent(joinCode, "PLAYER_REMOVED"));
            } else {
                log.warn("플레이어 제거 실패 (이미 없음): joinCode={}, playerName={}", joinCode, playerName);
            }

        } catch (Exception e) {
            log.error("방에서 플레이어 제거 실패: joinCode={}, playerName={}", joinCode, playerName, e);
        }
    }
}
