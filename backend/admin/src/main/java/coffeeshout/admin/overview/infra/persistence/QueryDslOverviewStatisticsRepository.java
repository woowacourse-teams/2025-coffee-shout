package coffeeshout.admin.overview.infra.persistence;

import coffeeshout.admin.overview.domain.DailyTrendPoint;
import coffeeshout.admin.overview.domain.OverviewStatisticsRepository;
import coffeeshout.admin.overview.domain.RoomFunnel;
import coffeeshout.minigame.infra.persistence.QMiniGameResultEntity;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.infra.persistence.QPlayerEntity;
import coffeeshout.room.infra.persistence.QRoomEntity;
import coffeeshout.user.infra.persistence.QUserEntity;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueryDslOverviewStatisticsRepository implements OverviewStatisticsRepository {

    private static final QRoomEntity ROOM = QRoomEntity.roomEntity;
    private static final QPlayerEntity PLAYER = QPlayerEntity.playerEntity;
    private static final QUserEntity USER = QUserEntity.userEntity;
    private static final QMiniGameResultEntity MINI_GAME_RESULT =
            QMiniGameResultEntity.miniGameResultEntity;

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

    /**
     * 날짜별 방 생성, 완주, 참여자 수.
     *
     * <p>방과 참여자를 한 쿼리로 조인하지 않는다. 카디널리티가 달라 참여자 수만큼 방이
     * 부풀어 생성 수가 틀린다.
     */
    @Override
    public List<DailyTrendPoint> findDailyTrend(LocalDateTime from, LocalDateTime to) {
        final DateTemplate<Date> roomDate = dateOf(ROOM.createdAt);

        // 날짜와 상태로 함께 묶어 한 번에 읽고 자바에서 접는다. 총합과 완주를 따로 조회하면
        // 그사이 데이터가 바뀌어 생성보다 완주가 많은 날이 생길 수 있다.
        // 행 수는 (날짜 x 상태 5개)라 14일이면 최대 70행이다.
        final Map<LocalDate, RoomCounts> roomsByDate = new HashMap<>();
        for (Tuple row : queryFactory
                .select(roomDate, ROOM.roomStatus, ROOM.count())
                .from(ROOM)
                .where(ROOM.createdAt.goe(from), ROOM.createdAt.lt(to))
                .groupBy(roomDate, ROOM.roomStatus)
                .fetch()) {
            final long count = nullToZero(row.get(ROOM.count()));
            final long completed = row.get(ROOM.roomStatus) == RoomState.DONE ? count : 0;
            roomsByDate.merge(
                    toLocalDate(row.get(roomDate)), new RoomCounts(count, completed), RoomCounts::plus);
        }

        final DateTemplate<Date> playerDate = dateOf(PLAYER.createdAt);
        final Map<LocalDate, Long> playersByDate = new HashMap<>();
        for (Tuple row : queryFactory
                .select(playerDate, PLAYER.count())
                .from(PLAYER)
                .where(PLAYER.createdAt.goe(from), PLAYER.createdAt.lt(to))
                .groupBy(playerDate)
                .fetch()) {
            playersByDate.put(toLocalDate(row.get(playerDate)), nullToZero(row.get(PLAYER.count())));
        }

        return Stream.concat(roomsByDate.keySet().stream(), playersByDate.keySet().stream())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(date -> new DailyTrendPoint(
                        date,
                        roomsByDate.getOrDefault(date, RoomCounts.EMPTY).created(),
                        roomsByDate.getOrDefault(date, RoomCounts.EMPTY).completed(),
                        playersByDate.getOrDefault(date, 0L)))
                .toList();
    }

    @Override
    public List<GamePlayCount> countPlaysByGame(LocalDateTime from, LocalDateTime to) {
        // mini_game_play 에는 시각이 없다. 결과(mini_game_result)의 시각으로 기간을 자르고
        // 판 단위로 센다. 한 판에 참가자 수만큼 결과가 생기므로 distinct 가 없으면
        // 4명짜리 한 판이 4판으로 세진다.
        return queryFactory
                .select(MINI_GAME_RESULT.miniGameType, MINI_GAME_RESULT.miniGamePlay.id.countDistinct())
                .from(MINI_GAME_RESULT)
                .where(MINI_GAME_RESULT.createdAt.goe(from), MINI_GAME_RESULT.createdAt.lt(to))
                .groupBy(MINI_GAME_RESULT.miniGameType)
                .fetch()
                .stream()
                .map(row -> new GamePlayCount(
                        row.get(MINI_GAME_RESULT.miniGameType),
                        nullToZero(row.get(1, Long.class))))
                .toList();
    }

    /**
     * MySQL {@code DATE()} 는 JDBC 로 {@code java.sql.Date} 를 돌려준다. LocalDate 로 선언하면
     * 조회 시점에 ClassCastException 이 난다. 통합 테스트에서만 드러나는 어긋남이라
     * 타입을 그대로 받고 경계에서 변환한다.
     *
     * <p>{@code DATE()} 를 쓰는 것은 테스트도 운영도 MySQL 8 이라 방언 차이가 없고,
     * 기간 전체를 끌어와 자바에서 묶는 것보다 훨씬 적게 읽기 때문이다.
     */
    private static DateTemplate<Date> dateOf(DateTimePath<LocalDateTime> path) {
        return Expressions.dateTemplate(Date.class, "DATE({0})", path);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private record RoomCounts(long created, long completed) {
        static final RoomCounts EMPTY = new RoomCounts(0, 0);

        RoomCounts plus(RoomCounts other) {
            return new RoomCounts(created + other.created, completed + other.completed);
        }
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
