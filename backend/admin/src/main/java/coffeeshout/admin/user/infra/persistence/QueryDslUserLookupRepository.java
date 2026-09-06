package coffeeshout.admin.user.infra.persistence;

import coffeeshout.admin.user.domain.UserActivity;
import coffeeshout.admin.user.domain.UserLookupRepository;
import coffeeshout.admin.user.domain.UserSummary;
import coffeeshout.room.infra.persistence.QPlayerEntity;
import coffeeshout.room.infra.persistence.QRouletteResultEntity;
import coffeeshout.user.infra.persistence.QOAuthAccountEntity;
import coffeeshout.user.infra.persistence.QUserEntity;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueryDslUserLookupRepository implements UserLookupRepository {

    private static final QUserEntity USER = QUserEntity.userEntity;
    private static final QPlayerEntity PLAYER = QPlayerEntity.playerEntity;
    private static final QRouletteResultEntity ROULETTE = QRouletteResultEntity.rouletteResultEntity;
    private static final QOAuthAccountEntity OAUTH = QOAuthAccountEntity.oAuthAccountEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserSummary> search(String keyword, Pageable pageable) {
        // 탈퇴 필터가 없다. @SQLRestriction 이 이미 탈퇴 회원을 걸러내 필터를 둬도 의미가 없다.
        final BooleanExpression condition = keywordMatches(keyword);

        final List<UserSummary> content = queryFactory
                .select(summaryProjection())
                .from(USER)
                .where(condition)
                .orderBy(USER.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final Long total = queryFactory.select(USER.count()).from(USER).where(condition).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Optional<UserSummary> findById(Long userId) {
        return Optional.ofNullable(queryFactory
                .select(summaryProjection())
                .from(USER)
                .where(USER.id.eq(userId))
                .fetchOne());
    }

    @Override
    public UserActivity findActivity(Long userId) {
        final Long roomCount = queryFactory
                .select(PLAYER.roomSession.id.countDistinct())
                .from(PLAYER)
                .where(PLAYER.userId.eq(userId))
                .fetchOne();

        final Long winCount = queryFactory
                .select(ROULETTE.count())
                .from(ROULETTE)
                .join(ROULETTE.winner, PLAYER)
                .where(PLAYER.userId.eq(userId))
                .fetchOne();

        return new UserActivity(nullToZero(roomCount), nullToZero(winCount));
    }

    @Override
    public List<String> findProviders(Long userId) {
        return queryFactory
                .select(OAUTH.provider)
                .from(OAUTH)
                .where(OAUTH.user.id.eq(userId))
                .orderBy(OAUTH.linkedAt.asc())
                .fetch();
    }

    private static ConstructorExpression<UserSummary> summaryProjection() {
        return Projections.constructor(UserSummary.class,
                USER.id, USER.userCode, USER.nickname, USER.createdAt);
    }

    /**
     * 닉네임 부분 일치 또는 유저코드 완전 일치.
     *
     * <p>유저코드는 완전 일치만 본다. 5자짜리 코드에 부분 일치를 걸면 아무 두 글자로도
     * 수십 명이 걸려 나와 검색이 쓸모없어진다.
     */
    private static BooleanExpression keywordMatches(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        final String trimmed = keyword.trim();
        return USER.nickname.contains(trimmed).or(USER.userCode.eq(trimmed.toUpperCase()));
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
