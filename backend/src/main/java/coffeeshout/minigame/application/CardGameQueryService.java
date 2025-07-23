package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameQueryService {

    private final CardGameRepository cardGameRepository;

    public CardGame getCardGame(Long roomId) {
        return cardGameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
    }
}
