package coffeeshout.room.application;

import coffeeshout.room.infra.persistence.DashboardQueryRepository;
import coffeeshout.room.ui.response.GamePlayCountResponse;
import coffeeshout.room.ui.response.LowestProbabilityWinnerResponse;
import coffeeshout.room.ui.response.TopWinnerResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    public List<TopWinnerResponse> getTop5Winners() {
        final LocalDateTime startOfMonth = getStartOfMonth();
        final LocalDateTime endOfMonth = getEndOfMonth();

        return dashboardQueryRepository.findTop5WinnersByMonth(startOfMonth, endOfMonth);
    }

    public LowestProbabilityWinnerResponse getLowestProbabilityWinner() {
        final LocalDateTime startOfMonth = getStartOfMonth();
        final LocalDateTime endOfMonth = getEndOfMonth();

        return dashboardQueryRepository.findLowestProbabilityWinner(startOfMonth, endOfMonth)
                .orElseThrow(() -> new IllegalStateException("이번달 당첨 기록이 없습니다"));
    }

    public List<GamePlayCountResponse> getGamePlayCounts() {
        final LocalDateTime startOfMonth = getStartOfMonth();
        final LocalDateTime endOfMonth = getEndOfMonth();

        return dashboardQueryRepository.findGamePlayCountByMonth(startOfMonth, endOfMonth);
    }

    private LocalDateTime getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    private LocalDateTime getEndOfMonth() {
        return getStartOfMonth().plusMonths(1);
    }
}
