package coffeeshout.room.domain.event;

import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateEvent {

    private String eventId;
    private String hostName;
    private SelectedMenuRequest selectedMenuRequest;
    private String joinCode;
    private LocalDateTime timestamp;

    public static RoomCreateEvent create(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode) {
        return new RoomCreateEvent(
                UUID.randomUUID().toString(),
                hostName,
                selectedMenuRequest,
                joinCode,
                LocalDateTime.now()
        );
    }
}
