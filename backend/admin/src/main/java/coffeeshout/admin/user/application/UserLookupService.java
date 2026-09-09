package coffeeshout.admin.user.application;

import coffeeshout.admin.user.domain.ProviderCount;
import coffeeshout.admin.user.domain.UserActivity;
import coffeeshout.admin.user.domain.UserLookupRepository;
import coffeeshout.admin.user.domain.UserSummary;
import coffeeshout.global.exception.GlobalErrorCode;
import coffeeshout.global.exception.custom.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLookupService {

    private static final int PAGE_SIZE = 20;

    private final UserLookupRepository userLookupRepository;

    public Page<UserSummary> search(String keyword, int page) {
        return userLookupRepository.search(keyword, PageRequest.of(page, PAGE_SIZE));
    }

    /** 소셜 제공자 분포. 활성 회원 수를 함께 준다 - 연결 수의 합과 회원 수는 다르다. */
    public ProviderStats findProviderStats() {
        return new ProviderStats(userLookupRepository.countUsers(), userLookupRepository.countByProvider());
    }

    public UserDetail findDetail(Long userId) {
        final UserSummary summary = userLookupRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_EXIST, "유저를 찾을 수 없습니다: " + userId));

        return new UserDetail(
                summary, userLookupRepository.findProviders(userId), userLookupRepository.findActivity(userId));
    }

    public record UserDetail(UserSummary summary, List<String> providers, UserActivity activity) {}

    public record ProviderStats(long userCount, List<ProviderCount> counts) {}
}
