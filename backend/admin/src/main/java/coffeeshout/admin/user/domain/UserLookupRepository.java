package coffeeshout.admin.user.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <b>활성 회원만 조회된다.</b> {@code UserEntity}의 {@code @SQLRestriction("deleted_at IS NULL")}
 * 때문에 탈퇴 회원은 JPA 경로로는 보이지 않는다.
 *
 * <p>탈퇴 회원 조회가 필요해지면 네이티브 쿼리로 우회해야 한다
 * ({@code UserJpaRepository.findByIdIgnoringDeletedAt}가 같은 이유로 그렇게 되어 있다).
 * 지금은 그 요구가 없어 넣지 않았다. 전역 규칙을 우회하는 경로는 필요해질 때 그 맥락에서 연다.
 */
public interface UserLookupRepository {

    /**
     * @param keyword 닉네임 부분 일치 또는 유저코드 완전 일치. 비어 있으면 최근 가입부터 전체.
     */
    Page<UserSummary> search(String keyword, Pageable pageable);

    Optional<UserSummary> findById(Long userId);

    UserActivity findActivity(Long userId);

    /** 이 유저가 쓰는 소셜 제공자. 여러 개를 연결할 수 있다. */
    List<String> findProviders(Long userId);
}
