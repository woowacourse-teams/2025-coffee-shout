package coffeeshout.room.application;

import coffeeshout.minigame.domain.dto.MiniGameCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomUpdateEventListener {

    private final RoomService roomService;

    @EventListener
    public void handleApplyMiniGameResult(MiniGameCompletedEvent event) {
        roomService.applyMiniGameResult(event.joinCode().getValue(), event.miniGameResult());
    }
}
