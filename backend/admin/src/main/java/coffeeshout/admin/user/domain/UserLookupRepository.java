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

    /** 활성 회원 수. 제공자별 연결 수의 합과 다르다 - 한 사람이 여러 소셜을 연결할 수 있다. */
    long countUsers();

    /**
     * 제공자별 연결 수. 연결이 하나도 없는 제공자는 결과에 나오지 않는다.
     *
     * <p>화면이 그 빈자리를 채운다. 여기서 0짜리 행을 만들어 주려면 저장소가 제공자
     * 목록을 알아야 하는데, 그건 {@code :user} 모듈의 enum 이고 조회 저장소가 알 일이 아니다.
     */
    List<ProviderCount> countByProvider();
}
