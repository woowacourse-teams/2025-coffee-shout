package coffeeshout.minigame.domain.cardgame.service;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.repository.CardGameRepository;
import coffeeshout.room.domain.JoinCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameCommandService {

    private final CardGameRepository cardGameRepository;

    public CardGame save(CardGame cardGame) {
        return cardGameRepository.save(cardGame);
    }
    // TODO 삭제 시점 고민하기
    public void delete(@NonNull JoinCode joinCode) {
        cardGameRepository.deleteByJoinCode(joinCode);
    }
}
