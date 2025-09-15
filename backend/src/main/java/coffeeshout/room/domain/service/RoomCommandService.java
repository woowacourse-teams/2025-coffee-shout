package coffeeshout.room.domain.service;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.room.RoomCreatedEvent;
import coffeeshout.global.redis.event.room.RoomDeletedEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCommandService {

    private final RoomRepository roomRepository;
    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;

    public Room save(Room room) {
        Room saved = roomRepository.save(room);

        // 새로 생성된 방인지 확인 (ID나 생성 여부로 판단)
        // 이 부분은 Room이 새로 생성되었는지 확인하는 로직이 필요함
        // 현재는 단순히 모든 save를 방 생성으로 처리
        publishRoomCreated(saved);

        return saved;
    }

    public void delete(@NonNull JoinCode joinCode) {
        roomRepository.deleteByJoinCode(joinCode);

        // 방 삭제 이벤트 발행
        messagePublisher.publishRoomDeleted(new RoomDeletedEvent(
                joinCode.getValue(),
                instanceConfig.getInstanceId()
        ));
    }

    private void publishRoomCreated(Room room) {
        try {
            final Player host = room.getHost(); // 첫 번째 플레이어가 호스트

            messagePublisher.publishRoomCreated(new RoomCreatedEvent(
                    room.getJoinCode().getValue(),
                    host.getName().value(),
                    host.getSelectedMenu(),
                    room.getJoinCode().getQrCodeUrl(), // JoinCode에 getQrCodeUrl() 메서드가 있다고 가정
                    instanceConfig.getInstanceId()
            ));
        } catch (Exception e) {
            // 로그만 남기고 실패해도 계속 진행
            // log.error("방 생성 이벤트 발행 실패: {}", e.getMessage(), e);
        }
    }
}
