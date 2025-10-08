package coffeeshout.room.infra.persistence;

import coffeeshout.room.ui.response.TopWinnerResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<TopWinnerResponse> findTop5WinnersByMonth(LocalDateTime startDate, LocalDateTime endDate) {
        final QRouletteResultEntity r = QRouletteResultEntity.rouletteResultEntity;
        final QPlayerEntity p = QPlayerEntity.playerEntity;

        return queryFactory
                .select(Projections.constructor(
                        TopWinnerResponse.class,
                        p.playerName,
                        r.count()
                ))
                .from(r)
                .join(r.winner, p)
                .where(r.createdAt.between(startDate, endDate))
                .groupBy(p.playerName)
                .orderBy(r.count().desc())
                .limit(5)
                .fetch();
    }
}
