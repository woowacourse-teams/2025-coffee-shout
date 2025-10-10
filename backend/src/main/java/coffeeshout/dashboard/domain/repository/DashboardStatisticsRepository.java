package coffeeshout.dashboard.domain.repository;

import coffeeshout.dashboard.ui.response.GamePlayCountResponse;
import coffeeshout.dashboard.ui.response.LowestProbabilityWinnerResponse;
import coffeeshout.dashboard.ui.response.TopWinnerResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardStatisticsRepository {

    List<TopWinnerResponse> findTopWinnersByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
    );

    Optional<LowestProbabilityWinnerResponse> findLowestProbabilityWinner(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
    );

    List<GamePlayCountResponse> findGamePlayCountByMonth(LocalDateTime startDate, LocalDateTime endDate);
}
