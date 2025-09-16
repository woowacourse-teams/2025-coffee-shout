package coffeeshout.room.application;

import coffeeshout.room.domain.event.RoomUpdateEvent;
import coffeeshout.room.domain.service.RoomCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomUpdateEventListener {

    private final RoomCommandService roomCommandService;

    @EventListener
    public void handleRoomUpdate(RoomUpdateEvent event) {
        try {
            roomCommandService.save(event.room());
            log.debug("Room saved automatically: {}", event.room().getJoinCode().getValue());
        } catch (Exception e) {
            log.error("Failed to save room: {}", event.room().getJoinCode().getValue(), e);
        }
    }
}
