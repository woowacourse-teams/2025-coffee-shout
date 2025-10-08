package coffeeshout.room.application;

import coffeeshout.room.infra.persistence.DashboardQueryRepository;
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
        final LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        final LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        return dashboardQueryRepository.findTop5WinnersByMonth(startOfMonth, endOfMonth);
    }

    public LowestProbabilityWinnerResponse getLowestProbabilityWinner() {
        final LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        final LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        return dashboardQueryRepository.findLowestProbabilityWinner(startOfMonth, endOfMonth)
                .orElseThrow(() -> new IllegalStateException("이번달 당첨 기록이 없습니다"));
    }
}
