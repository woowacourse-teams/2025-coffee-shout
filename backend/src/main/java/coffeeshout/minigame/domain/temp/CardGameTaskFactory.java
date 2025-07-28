package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGame;

public class CardGameTaskFactory {

    public static Runnable play(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.startRound();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
            cardGame.assignRandomCardsToUnselectedPlayers();
        };
    }

    public static Runnable scoreBoard(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeScoreBoardState();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
        };
    }

    public static Runnable loading(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeLoadingState();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
        };
    }

    public static Runnable done(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeDoneState();
            sendMessage.run();
            cardGame.assignRandomCardsToUnselectedPlayers();
            sleep(cardGame.getState().getDuration());
        };
    }

    public static void sleep(long milliSecond) {
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
