package coffeeshout.room.application.event;

import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerKickEvent;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.QrCodeStatusEvent;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Room 이벤트 로컬 핸들러
 * - 로컬 인스턴스에서만 처리하는 이벤트
 * - 로컬 캐시 업데이트, 메트릭 수집 등
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventLocalHandler {

    /**
     * 방 생성 이벤트 - 트랜잭션 커밋 후 처리
     *
     * 이유:
     * - DB에 Room이 확실히 저장된 후에만 처리
     * - 롤백 시 QR 생성, 캐시 업데이트 등 부작용 방지
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomCreate(RoomCreateEvent event) {
        log.info("[로컬][트랜잭션 커밋 후] 방 생성 이벤트 처리: eventId={}, joinCode={}",
            event.eventId(), event.joinCode());

        // 로컬 처리 (예: 캐시 업데이트, 메트릭 수집)
        // 이 시점에는 DB에 확실히 저장됨
    }

    /**
     * QR 코드 상태 이벤트 - 즉시 처리
     *
     * 이유:
     * - QR 생성 완료 즉시 사용자에게 알림 필요
     * - @Async로 실행되는 QrCodeService에서 발행
     * - 트랜잭션 컨텍스트 없음
     */
    @EventListener
    public void handleQrCodeStatus(QrCodeStatusEvent event) {
        log.info("[로컬][즉시] QR 코드 상태 이벤트 처리: eventId={}, joinCode={}, status={}",
            event.eventId(), event.joinCode(), event.status());

        // 즉시 처리 (예: 로컬 캐시 업데이트)
    }

    /**
     * 플레이어 강퇴 이벤트 - 즉시 처리
     *
     * 이유:
     * - 강퇴 즉시 사용자에게 알림
     * - 빠른 피드백 필요
     */
    @EventListener
    public void handlePlayerKick(PlayerKickEvent event) {
        log.info("[로컬][즉시] 플레이어 강퇴 이벤트 처리: eventId={}, joinCode={}, playerName={}",
            event.eventId(), event.joinCode(), event.playerName());

        // 즉시 처리
    }

    /**
     * 방 입장 이벤트 - 즉시 처리
     *
     * 이유:
     * - Stream Consumer에서 발행
     * - 이미 joinGuest가 완료된 상태
     * - 즉각적인 알림 필요
     *
     * 참고: 실제 비즈니스 로직은 RoomJoinEventHandler에서 처리
     */
    @EventListener
    public void handleRoomJoin(RoomJoinEvent event) {
        log.info("[로컬][즉시] 방 입장 이벤트 처리: eventId={}, joinCode={}, guestName={}",
            event.eventId(), event.joinCode(), event.guestName());

        // 즉시 처리 (메트릭, 로깅 등)
    }

    /**
     * 룰렛 스핀 이벤트 - 즉시 처리
     *
     * 이유:
     * - 룰렛 결과 즉시 전파
     * - 실시간 게임 경험
     */
    @EventListener
    public void handleRouletteSpin(RouletteSpinEvent event) {
        log.info("[로컬][즉시] 룰렛 스핀 이벤트 처리: eventId={}, joinCode={}",
            event.eventId(), event.joinCode());

        // 즉시 처리
    }

    /**
     * 룰렛 표시 이벤트 - 즉시 처리
     */
    @EventListener
    public void handleRouletteShow(RouletteShowEvent event) {
        log.info("[로컬][즉시] 룰렛 표시 이벤트 처리: eventId={}, joinCode={}",
            event.eventId(), event.joinCode());

        // 즉시 처리
    }

    /**
     * 플레이어 준비 상태 변경 - 즉시 처리
     *
     * 이유:
     * - 준비 상태 즉시 반영
     * - 다른 플레이어에게 빠른 피드백
     */
    @EventListener
    public void handlePlayerReady(PlayerReadyEvent event) {
        log.info("[로컬][즉시] 플레이어 준비 상태 이벤트 처리: eventId={}, joinCode={}, playerName={}, isReady={}",
            event.eventId(), event.joinCode(), event.playerName(), event.isReady());

        // 즉시 처리
    }

    /**
     * 플레이어 목록 업데이트 - 즉시 처리
     */
    @EventListener
    public void handlePlayerListUpdate(PlayerListUpdateEvent event) {
        log.info("[로컬][즉시] 플레이어 목록 업데이트 이벤트 처리: eventId={}, joinCode={}",
            event.eventId(), event.joinCode());

        // 즉시 처리
    }

    /**
     * 미니게임 선택 - 즉시 처리
     */
    @EventListener
    public void handleMiniGameSelect(MiniGameSelectEvent event) {
        log.info("[로컬][즉시] 미니게임 선택 이벤트 처리: eventId={}, joinCode={}, hostName={}, types={}",
            event.eventId(), event.joinCode(), event.hostName(), event.miniGameTypes());

        // 즉시 처리
    }
}
