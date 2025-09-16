package coffeeshout.room.domain.event;

import coffeeshout.room.ui.request.SelectedMenuRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class RoomCreateEvent {

    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("hostName")
    private String hostName;
    
    @JsonProperty("selectedMenuRequest")
    private SelectedMenuRequest selectedMenuRequest;
    
    @JsonProperty("jode")
    private String joinCode;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("hostName")
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("selectedMenuRequest")
    public SelectedMenuRequest getSelectedMenuRequest() {
        return selectedMenuRequest;
    }

    @JsonProperty("jode")
    public String getJoinCode() {
        return joinCode;
    }

    @JsonProperty("timestamp")
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setSelectedMenuRequest(SelectedMenuRequest selectedMenuRequest) {
        this.selectedMenuRequest = selectedMenuRequest;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

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
