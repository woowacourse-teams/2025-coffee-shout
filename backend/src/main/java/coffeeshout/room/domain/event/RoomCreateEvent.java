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
public class RoomCreateEvent {

    private String eventId;
    private String hostName;
    private SelectedMenuRequest selectedMenuRequest;
    private String joinCode;
    private LocalDateTime timestamp;

    public static RoomCreateEvent create(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode) {
        final RoomCreateEvent event = new RoomCreateEvent();
        event.eventId = UUID.randomUUID().toString();
        event.hostName = hostName;
        event.selectedMenuRequest = selectedMenuRequest;
        event.joinCode = joinCode;
        event.timestamp = LocalDateTime.now();
        return event;
    }
}
