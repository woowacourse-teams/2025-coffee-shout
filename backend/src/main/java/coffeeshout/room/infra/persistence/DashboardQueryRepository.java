package coffeeshout.room.infra.persistence;

import coffeeshout.room.ui.response.LowestProbabilityWinnerResponse;
import coffeeshout.room.ui.response.TopWinnerResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    public Optional<LowestProbabilityWinnerResponse> findLowestProbabilityWinner(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        final QRouletteResultEntity r = QRouletteResultEntity.rouletteResultEntity;
        final QPlayerEntity p = QPlayerEntity.playerEntity;

        // 1. 최소 확률 찾기
        final Integer minProbability = queryFactory
                .select(r.winnerProbability.min())
                .from(r)
                .where(r.createdAt.between(startDate, endDate))
                .fetchOne();

        if (minProbability == null) {
            return Optional.empty();
        }

        // 2. 최소 확률로 당첨된 닉네임들 조회 (5명 제한)
        final List<String> nicknames = queryFactory
                .select(p.playerName)
                .from(r)
                .join(r.winner, p)
                .where(
                        r.createdAt.between(startDate, endDate),
                        r.winnerProbability.eq(minProbability)
                )
                .distinct()
                .limit(5)
                .fetch();

        return Optional.of(new LowestProbabilityWinnerResponse(minProbability, nicknames));
    }
}
