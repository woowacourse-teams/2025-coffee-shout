package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.ui.request.RoomCreateRequest;
import coffeeshout.room.ui.response.MiniGameResponse;
import coffeeshout.room.ui.response.RoomCreateResponse;
import coffeeshout.room.ui.response.RoomEnterResponse;
import java.util.List;
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
        final Room room = roomService.createRoom(request.hostName(), request.MenuId());

        return ResponseEntity.ok(RoomCreateResponse.from(room));
    }

    @PostMapping("/enter")
    public ResponseEntity<RoomEnterResponse> enterRoom(@RequestBody RoomEnterRequest request) {
        final Room room = roomService.enterRoom(request.joinCode(), request.guestName(), request.menuId());

        return ResponseEntity.ok(RoomEnterResponse.from(room));
    }

    @GetMapping("/minigames")
    public ResponseEntity<List<MiniGameResponse>> getMiniGames() {
        final List<MiniGameResponse> responses = roomService.getAllMiniGames().stream()
                .map(MiniGameResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
