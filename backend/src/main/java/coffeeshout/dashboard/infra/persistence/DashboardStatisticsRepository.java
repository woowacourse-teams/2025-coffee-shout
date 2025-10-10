package coffeeshout.dashboard.infra.persistence;

import coffeeshout.dashboard.ui.response.GamePlayCountResponse;
import coffeeshout.dashboard.ui.response.LowestProbabilityWinnerResponse;
import coffeeshout.dashboard.ui.response.TopWinnerResponse;
import coffeeshout.minigame.infra.persistence.QMiniGameEntity;
import coffeeshout.room.infra.persistence.QPlayerEntity;
import coffeeshout.room.infra.persistence.QRoomEntity;
import coffeeshout.room.infra.persistence.QRouletteResultEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DashboardStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    public List<TopWinnerResponse> findTopWinnersByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
    ) {
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
                .limit(limit)
                .fetch();
    }

    public Optional<LowestProbabilityWinnerResponse> findLowestProbabilityWinner(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit
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

        // 2. 최소 확률로 당첨된 닉네임들 조회
        final List<String> nicknames = queryFactory
                .select(p.playerName)
                .from(r)
                .join(r.winner, p)
                .where(
                        r.createdAt.between(startDate, endDate),
                        r.winnerProbability.eq(minProbability)
                )
                .distinct()
                .orderBy(p.playerName.asc())
                .limit(limit)
                .fetch();

        return Optional.of(new LowestProbabilityWinnerResponse(minProbability, nicknames));
    }

    public List<GamePlayCountResponse> findGamePlayCountByMonth(LocalDateTime startDate, LocalDateTime endDate) {
        final QMiniGameEntity m = QMiniGameEntity.miniGameEntity;
        final QRoomEntity room = QRoomEntity.roomEntity;

        return queryFactory
                .select(Projections.constructor(
                        GamePlayCountResponse.class,
                        m.miniGameType,
                        m.count()
                ))
                .from(m)
                .join(m.roomSession, room)
                .where(room.createdAt.between(startDate, endDate))
                .groupBy(m.miniGameType)
                .orderBy(m.count().desc())
                .fetch();
    }
}
