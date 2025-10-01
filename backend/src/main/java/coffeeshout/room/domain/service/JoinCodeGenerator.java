package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinCodeGenerator {

    // TODO Redis Repository 필요 분산 환경에서 하나만 처리되도록 해야함
    private final RoomRepository roomRepository;

    public JoinCode generate() {
        return Stream.generate(JoinCode::generate)
                .dropWhile(roomRepository::existsByJoinCode)
                .limit(100) // 안전망
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("입장 코드 생성이 실패했습니다."));
    }
}
