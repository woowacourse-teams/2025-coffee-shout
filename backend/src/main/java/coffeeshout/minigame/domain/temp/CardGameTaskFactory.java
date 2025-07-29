package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.Room;

public class CardGameTaskFactory {

    private final ThreadSleeper threadSleeper;

    public CardGameTaskFactory(ThreadSleeper threadSleeper) {
        this.threadSleeper = threadSleeper;
    }

    public Runnable play(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.startRound();
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
            cardGame.changeLoadingState();
            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
        };
    }

    public Runnable done(Room room, CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeDoneState();
            room.applyMiniGameResult(cardGame.getResult());
            sendMessage.run();
            threadSleeper.sleep(cardGame.getState().getDuration());
        };
    }
}
