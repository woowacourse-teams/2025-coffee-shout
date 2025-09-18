package coffeeshout.room.ui;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.application.messaging.RoomBroadcastStreamProducer;
import coffeeshout.room.domain.Room;
import coffeeshout.room.ui.request.RoomEnterRequest;
import coffeeshout.room.ui.response.GuestNameExistResponse;
import coffeeshout.room.ui.response.JoinCodeExistResponse;
import coffeeshout.room.ui.response.RoomCreateResponse;
import coffeeshout.room.ui.response.RoomEnterResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomRestController {

    private final RoomService roomService;
    private final RoomBroadcastStreamProducer roomBroadcastStreamProducer;

    @PostMapping
    public ResponseEntity<RoomCreateResponse> createRoom(@RequestBody RoomEnterRequest request) {
        final Room room = roomService.createRoom(request.playerName(), request.menu());

        return ResponseEntity.ok(RoomCreateResponse.from(room));
    }

    @PostMapping("/{joinCode}")
    public CompletableFuture<ResponseEntity<RoomEnterResponse>> enterRoom(
            @PathVariable String joinCode,
            @RequestBody RoomEnterRequest request
    ) {
        // TODO Subscribe에서 예외가 발생할 수 있어서 처리해야 한다.
        return roomBroadcastStreamProducer.broadcastEnterRoomSync(joinCode, request.playerName(), request.menu())
                .thenApply(v -> {
                    Room room = roomService.getRoom(joinCode);
                    log.info("Entered room: joinCode={}, playerName={}", joinCode, request.playerName());
                    return ResponseEntity.ok(RoomEnterResponse.from(room));
                }).orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(throwable -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/check-joinCode")
    public ResponseEntity<JoinCodeExistResponse> checkJoinCode(@RequestParam String joinCode) {
        final boolean exists = roomService.roomExists(joinCode);

        return ResponseEntity.ok(JoinCodeExistResponse.from(exists));
    }

    @GetMapping("/check-guestName")
    public ResponseEntity<GuestNameExistResponse> checkGuestName(
            @RequestParam String joinCode,
            @RequestParam String guestName
    ) {
        final boolean isDuplicated = roomService.isGuestNameDuplicated(joinCode, guestName);

        return ResponseEntity.ok(GuestNameExistResponse.from(isDuplicated));
    }

    @GetMapping("/minigames")
    public ResponseEntity<List<MiniGameType>> getMiniGames() {
        final List<MiniGameType> responses = roomService.getAllMiniGames();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/minigames/selected")
    public ResponseEntity<List<MiniGameType>> getSelectedMiniGames(@RequestParam String joinCode){
        List<MiniGameType> result = roomService.getSelectedMiniGames(joinCode);

        return ResponseEntity.ok(result);
    }
}
