package coffeeshout.room.ui;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.ui.request.RoomEnterRequest;
import coffeeshout.room.ui.response.GuestNameExistResponse;
import coffeeshout.room.ui.response.JoinCodeExistResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import coffeeshout.room.ui.response.RoomCreateResponse;
import coffeeshout.room.ui.response.RoomEnterResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public CompletableFuture<ResponseEntity<RoomCreateResponse>> createRoom(@RequestBody RoomEnterRequest request) {
        return roomService.createRoomAsync(request.playerName(), request.menu())
                .thenApply(room -> ResponseEntity.ok(RoomCreateResponse.from(room)))
                .exceptionally(throwable -> {
                    final Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    }
                    throw new RuntimeException("방 생성 실패", cause);
                });
    }

    @PostMapping("/{joinCode}")
    public CompletableFuture<ResponseEntity<RoomEnterResponse>> enterRoom(
            @PathVariable String joinCode,
            @RequestBody RoomEnterRequest request
    ) {
        final Room room = roomService.enterRoom(joinCode, request.playerName(), request.menu());

        return ResponseEntity.ok(RoomEnterResponse.from(room));
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

    @GetMapping("/probabilities")
    public ResponseEntity<List<ProbabilityResponse>> getProbabilities(@RequestParam String joinCode) {
        final List<ProbabilityResponse> responses = roomService.getProbabilities(joinCode);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/minigames")
    public ResponseEntity<List<MiniGameType>> getMiniGames() {
        final List<MiniGameType> responses = roomService.getAllMiniGames();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/minigames/selected")
    public ResponseEntity<List<MiniGameType>> getSelectedMiniGames(@RequestParam String joinCode) {
        final List<MiniGameType> result = roomService.getSelectedMiniGames(joinCode);

        return ResponseEntity.ok(result);
    }
}
