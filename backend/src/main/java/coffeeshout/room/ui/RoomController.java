package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.ui.request.RoomCreateRequest;
import coffeeshout.room.ui.response.RoomCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/rooms")
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestBody RoomCreateRequest request) {
        final Room room = roomService.createRoom(request.hostName(), request.MenuId());

        return ResponseEntity.ok(RoomCreateResponse.from(room));
    }
}
