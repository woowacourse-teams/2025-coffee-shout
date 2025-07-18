package coffeeshout.room.domain;

import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinCodeGenerator {

    private final RoomRepository roomRepository;

    public JoinCode generate() {
        JoinCode joinCode;
        do {
            joinCode = JoinCode.generate();
        } while (roomRepository.existsByJoinCode(joinCode));

        return joinCode;
    }
}
