package coffeeshout.room.domain.event;

import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RoomJoinEvent {

    private String eventId;
    private String joinCode;
    private String guestName;
    private SelectedMenuRequest selectedMenuRequest;
    private LocalDateTime timestamp;

    public static RoomJoinEvent create(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        final RoomJoinEvent event = new RoomJoinEvent();
        event.eventId = UUID.randomUUID().toString();
        event.joinCode = joinCode;
        event.guestName = guestName;
        event.selectedMenuRequest = selectedMenuRequest;
        event.timestamp = LocalDateTime.now();
        return event;
    }
}
