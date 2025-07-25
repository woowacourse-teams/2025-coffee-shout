package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGame;

public class CardGameTaskFactory {

    private static Task play(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.startRound();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
            cardGame.assignRandomCardsToUnselectedPlayers();
        };
    }

    private static Task scoreBoard(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeScoreBoardState();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
        };
    }

    private static Task loading(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeLoadingState();
            sendMessage.run();
            sleep(cardGame.getState().getDuration());
        };
    }

    private static Task done(CardGame cardGame, Runnable sendMessage) {
        return () -> {
            cardGame.changeDoneState();
            sendMessage.run();
            cardGame.assignRandomCardsToUnselectedPlayers();
            sleep(cardGame.getState().getDuration());
        };
    }

    private static void sleep(long milliSecond) {
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
