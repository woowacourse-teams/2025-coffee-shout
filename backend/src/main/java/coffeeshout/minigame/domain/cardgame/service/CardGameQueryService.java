package coffeeshout.minigame.domain.cardgame.service;

import coffeeshout.global.exception.GlobalErrorCode;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.repository.CardGameRepository;
import coffeeshout.room.domain.JoinCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameQueryService {

    private final CardGameRepository cardGameRepository;

    public CardGame getByJoinCode(@NonNull JoinCode joinCode) {
        return cardGameRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new NotExistElementException(GlobalErrorCode.NOT_EXIST, "카드게임이 존재하지 않습니다."));
    }
}
