package coffeeshout.room.ui;

import coffeeshout.room.application.DashboardService;
import coffeeshout.room.ui.response.LowestProbabilityWinnerResponse;
import coffeeshout.room.ui.response.TopWinnerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/top-winners")
    public ResponseEntity<List<TopWinnerResponse>> getTop5Winners() {
        return ResponseEntity.ok(dashboardService.getTop5Winners());
    }

    @GetMapping("/lowest-probability-winner")
    public ResponseEntity<LowestProbabilityWinnerResponse> getLowestProbabilityWinner() {
        return ResponseEntity.ok(dashboardService.getLowestProbabilityWinner());
    }
}
