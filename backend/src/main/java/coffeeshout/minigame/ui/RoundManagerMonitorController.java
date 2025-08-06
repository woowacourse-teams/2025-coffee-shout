package coffeeshout.minigame.ui;

import coffeeshout.minigame.application.CardGameService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RoundManager 상태 모니터링을 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/monitoring/round-manager")
@RequiredArgsConstructor
public class RoundManagerMonitorController {
    
    private final CardGameService cardGameService;
    
    /**
     * 현재 활성화된 방의 수를 반환합니다.
     */
    @GetMapping("/active-rooms")
    public Map<String, Object> getActiveRooms() {
        int activeRoomCount = cardGameService.getActiveRoomCount();
        
        return Map.of(
            "activeRoomCount", activeRoomCount,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * RoundManager 상태 정보를 반환합니다.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        int activeRoomCount = cardGameService.getActiveRoomCount();
        
        return Map.of(
            "status", "healthy",
            "activeRoomCount", activeRoomCount,
            "version", "v2.0",
            "timestamp", System.currentTimeMillis()
        );
    }
}
