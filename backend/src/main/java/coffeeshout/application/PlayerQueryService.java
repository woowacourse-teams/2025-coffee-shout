package coffeeshout.application;

import coffeeshout.domain.Player;
import coffeeshout.domain.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerQueryService {

    private final PlayerRepository playerRepository;

    public Player findById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
