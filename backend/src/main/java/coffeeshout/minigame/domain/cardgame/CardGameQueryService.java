package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.application.CardGameRepository;
import coffeeshout.room.domain.JoinCode;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameQueryService {

    private final CardGameRepository cardGameRepository;

    public CardGame getCardGame(JoinCode joinCode) {
        return cardGameRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
    }
}
