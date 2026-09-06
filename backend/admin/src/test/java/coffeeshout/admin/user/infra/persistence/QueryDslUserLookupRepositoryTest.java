package coffeeshout.admin.user.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.AdminModuleServiceTest;
import coffeeshout.admin.user.domain.UserActivity;
import coffeeshout.admin.user.domain.UserLookupRepository;
import coffeeshout.admin.user.domain.UserSummary;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.PlayerJpaRepository;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RoomJpaRepository;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.room.infra.persistence.RouletteResultJpaRepository;
import coffeeshout.user.infra.persistence.UserEntity;
import coffeeshout.user.infra.persistence.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@DisplayName("QueryDslUserLookupRepository")
class QueryDslUserLookupRepositoryTest extends AdminModuleServiceTest {

    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 20);

    @Autowired
    private UserLookupRepository userLookupRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private RouletteResultJpaRepository rouletteResultJpaRepository;

    @Nested
    class search {

        @Test
        void 닉네임_부분_일치로_찾는다() {
            userJpaRepository.save(new UserEntity("AB3CD", "김철수"));
            userJpaRepository.save(new UserEntity("XY4ZQ", "박영희"));

            assertThat(userLookupRepository.search("철수", FIRST_PAGE).getContent())
                    .extracting(UserSummary::nickname).containsExactly("김철수");
        }

        @Test
        void 유저코드는_완전_일치로만_찾는다() {
            // 5자짜리 코드에 부분 일치를 걸면 아무 두 글자로도 수십 명이 걸려 검색이 쓸모없어진다.
            userJpaRepository.save(new UserEntity("AB3CD", "철수"));

            assertThat(userLookupRepository.search("AB3CD", FIRST_PAGE).getContent())
                    .hasSize(1);
            assertThat(userLookupRepository.search("AB3", FIRST_PAGE).getContent())
                    .isEmpty();
        }

        @Test
        void 유저코드는_소문자로_검색해도_찾는다() {
            userJpaRepository.save(new UserEntity("AB3CD", "철수"));

            assertThat(userLookupRepository.search("ab3cd", FIRST_PAGE).getContent())
                    .hasSize(1);
        }

        @Test
        void 키워드가_비어_있으면_최근_가입부터_전체를_돌려준다() {
            userJpaRepository.save(new UserEntity("AB3CD", "철수"));
            userJpaRepository.save(new UserEntity("XY4ZQ", "영희"));

            assertThat(userLookupRepository.search("", FIRST_PAGE).getTotalElements())
                    .isEqualTo(2);
        }

        @Test
        void 탈퇴_회원은_조회되지_않는다() {
            // UserEntity 의 @SQLRestriction("deleted_at IS NULL") 이 모든 JPA 조회에 붙는다.
            // 백오피스가 활성 회원만 본다는 사실을 이 테스트가 고정한다.
            final UserEntity active = userJpaRepository.save(new UserEntity("AB3CD", "활성"));
            final UserEntity withdrawn = userJpaRepository.save(new UserEntity("XY4ZQ", "탈퇴"));
            withdrawn.softDelete();
            userJpaRepository.saveAndFlush(withdrawn);

            assertThat(userLookupRepository.search(null, FIRST_PAGE).getContent())
                    .extracting(UserSummary::id).containsExactly(active.getId());
        }
    }

    @Nested
    class findActivity {

        @Test
        void 참여한_방_수와_당첨_수를_센다() {
            final UserEntity user = userJpaRepository.save(new UserEntity("AB3CD", "철수"));
            final RoomEntity room1 = roomJpaRepository.save(new RoomEntity("AAAA"));
            final RoomEntity room2 = roomJpaRepository.save(new RoomEntity("BBBB"));
            final PlayerEntity p1 = playerJpaRepository.save(
                    new PlayerEntity(room1, "철수", PlayerType.HOST, user.getId()));
            playerJpaRepository.save(new PlayerEntity(room2, "철수", PlayerType.GUEST, user.getId()));
            rouletteResultJpaRepository.save(new RouletteResultEntity(room1, p1, 25));

            final UserActivity activity = userLookupRepository.findActivity(user.getId());

            assertThat(activity.roomCount()).isEqualTo(2);
            assertThat(activity.winCount()).isEqualTo(1);
            assertThat(activity.winRate()).isEqualTo(0.5);
        }

        @Test
        void 같은_방에_여러_행이_있어도_방은_한_번만_센다() {
            final UserEntity user = userJpaRepository.save(new UserEntity("AB3CD", "철수"));
            final RoomEntity room = roomJpaRepository.save(new RoomEntity("AAAA"));
            playerJpaRepository.save(new PlayerEntity(room, "철수", PlayerType.HOST, user.getId()));
            playerJpaRepository.save(new PlayerEntity(room, "철수2", PlayerType.GUEST, user.getId()));

            assertThat(userLookupRepository.findActivity(user.getId()).roomCount()).isEqualTo(1);
        }

        @Test
        void 참여_이력이_없으면_전부_0이다() {
            final UserEntity user = userJpaRepository.save(new UserEntity("AB3CD", "철수"));

            final UserActivity activity = userLookupRepository.findActivity(user.getId());

            assertThat(activity.roomCount()).isZero();
            assertThat(activity.winCount()).isZero();
            assertThat(activity.winRate()).isZero();
        }

        @Test
        void 다른_유저의_당첨은_세지_않는다() {
            final UserEntity user = userJpaRepository.save(new UserEntity("AB3CD", "철수"));
            final UserEntity other = userJpaRepository.save(new UserEntity("XY4ZQ", "영희"));
            final RoomEntity room = roomJpaRepository.save(new RoomEntity("AAAA"));
            playerJpaRepository.save(new PlayerEntity(room, "철수", PlayerType.HOST, user.getId()));
            final PlayerEntity otherPlayer = playerJpaRepository.save(
                    new PlayerEntity(room, "영희", PlayerType.GUEST, other.getId()));
            rouletteResultJpaRepository.save(new RouletteResultEntity(room, otherPlayer, 50));

            assertThat(userLookupRepository.findActivity(user.getId()).winCount()).isZero();
        }
    }

    @Nested
    class findProviders {

        @Test
        void 연결된_제공자가_없으면_빈_목록이다() {
            final UserEntity user = userJpaRepository.save(new UserEntity("AB3CD", "철수"));

            assertThat(userLookupRepository.findProviders(user.getId())).isEmpty();
        }
    }
}
