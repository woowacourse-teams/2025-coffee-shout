package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.repository.JoinCodeRepository;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinCodeGenerator {

    private final JoinCodeRepository joinCodeRepository;

    public JoinCode generate() {
        JoinCode joinCode = Stream.generate(JoinCode::generate)
                .dropWhile(joinCodeRepository::existsByJoinCode)
                .limit(100) // 안전망
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("입장 코드 생성이 실패했습니다."));
        return joinCodeRepository.save(joinCode);
    }
}
