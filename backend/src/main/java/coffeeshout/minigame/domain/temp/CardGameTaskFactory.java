package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.Room;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskFactory {

    private final ThreadSleeper threadSleeper;

    public CardGameTaskFactory(ThreadSleeper threadSleeper) {
        this.threadSleeper = threadSleeper;
    }

    public Runnable play(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.startPlay();
            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
            cardGame.assignRandomCardsToUnselectedPlayers();
        };
    }

    public Runnable scoreBoard(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeScoreBoardState();
            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
        };
    }

    public Runnable loading(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.startRound();
            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
        };
    }

    public Runnable done(Room room, CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeDoneState();
            MiniGameResult result = cardGame.getResult();
            room.applyMiniGameResult(result);

            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
        };
    }
}
