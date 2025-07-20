package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.RouletteRoom;
import coffeeshout.room.ui.request.RoomCreateRequest;
import coffeeshout.room.ui.response.RoomCreateResponse;
import coffeeshout.room.ui.response.RoomEnterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomRestController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestBody RoomCreateRequest request) {
        final RouletteRoom room = roomService.createRoom(request.hostName(), request.MenuId());

        return ResponseEntity.ok(RoomCreateResponse.from(room));
    }

    @GetMapping("/enter")
    public ResponseEntity<RoomEnterResponse> enterRoom(
            @RequestParam String joinCode,
            @RequestParam String guestName,
            @RequestParam Long menuId
    ) {
        final RouletteRoom room = roomService.enterRoom(joinCode, guestName, menuId);

        return ResponseEntity.ok(RoomEnterResponse.from(room));
    }
}
