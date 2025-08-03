package coffeeshout.fixture;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameScore;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import java.util.Map;

public class MiniGameDummy implements Playable {

    @Override
    public MiniGameResult getResult() {
        return MiniGameResult.from(getScores());
    }

    @Override
    public Map<Player, MiniGameScore> getScores() {
        return Map.of(
                PlayerFixture.호스트꾹이(), new CardGameScore(20),
                PlayerFixture.게스트루키(), new CardGameScore(-10)
        );
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }

    @Override
    public void startGame(List<Player> players) {
    }
}
