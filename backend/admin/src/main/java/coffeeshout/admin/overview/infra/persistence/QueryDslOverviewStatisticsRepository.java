package coffeeshout.admin.overview.infra.persistence;

import coffeeshout.admin.overview.domain.OverviewStatisticsRepository;
import coffeeshout.admin.overview.domain.RoomFunnel;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.infra.persistence.QPlayerEntity;
import coffeeshout.room.infra.persistence.QRoomEntity;
import coffeeshout.user.infra.persistence.QUserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueryDslOverviewStatisticsRepository implements OverviewStatisticsRepository {

    private static final QRoomEntity ROOM = QRoomEntity.roomEntity;
    private static final QPlayerEntity PLAYER = QPlayerEntity.playerEntity;
    private static final QUserEntity USER = QUserEntity.userEntity;

    /** 게임을 시작한 방. READY 를 벗어났다는 뜻이다. */
    private static final List<RoomState> STARTED =
            List.of(RoomState.PLAYING, RoomState.SCORE_BOARD, RoomState.ROULETTE, RoomState.DONE);

    private static final List<RoomState> ROULETTE_REACHED =
            List.of(RoomState.ROULETTE, RoomState.DONE);

    private final JPAQueryFactory queryFactory;

    @Override
    public RoomFunnel findFunnelBetween(LocalDateTime from, LocalDateTime to) {
        final long created = countRooms(from, to, null);
        if (created == 0) {
            // 방이 없으면 나머지도 전부 0이다. 쿼리를 네 번 더 돌릴 이유가 없다.
            return RoomFunnel.empty();
        }
        return new RoomFunnel(
                created,
                countJoinedRooms(from, to),
                countRooms(from, to, ROOM.roomStatus.in(STARTED)),
                countRooms(from, to, ROOM.roomStatus.in(ROULETTE_REACHED)),
                countRooms(from, to, ROOM.roomStatus.eq(RoomState.DONE)));
    }

    @Override
    public long countPlayersBetween(LocalDateTime from, LocalDateTime to) {
        return nullToZero(queryFactory
                .select(PLAYER.count())
                .from(PLAYER)
                .where(PLAYER.createdAt.goe(from), PLAYER.createdAt.lt(to))
                .fetchOne());
    }

    @Override
    public long countSignupsBetween(Instant from, Instant to) {
        return nullToZero(queryFactory
                .select(USER.count())
                .from(USER)
                .where(USER.createdAt.goe(from), USER.createdAt.lt(to))
                .fetchOne());
    }

    private long countRooms(LocalDateTime from, LocalDateTime to, BooleanExpression condition) {
        return nullToZero(queryFactory
                .select(ROOM.count())
                .from(ROOM)
                .where(ROOM.createdAt.goe(from), ROOM.createdAt.lt(to), condition)
                .fetchOne());
    }

    /**
     * 참여자가 2명 이상인 방. 방장 혼자 만들고 아무도 안 들어온 방을 걸러낸다.
     * 그 방들을 세면 퍼널 1단계가 부풀어 이탈 지점을 못 찾는다.
     */
    private long countJoinedRooms(LocalDateTime from, LocalDateTime to) {
        // GROUP BY + HAVING 의 결과 행 수가 곧 답이다. 바깥에서 한 번 더 세려면
        // 서브쿼리를 감싸야 하는데, 여기서는 행 수만 필요하므로 그대로 센다.
        return queryFactory
                .select(PLAYER.roomSession.id)
                .from(PLAYER)
                .join(PLAYER.roomSession, ROOM)
                .where(ROOM.createdAt.goe(from), ROOM.createdAt.lt(to))
                .groupBy(PLAYER.roomSession.id)
                .having(PLAYER.count().goe(2))
                .fetch()
                .size();
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
