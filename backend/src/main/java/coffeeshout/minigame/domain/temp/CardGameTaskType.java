package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum CardGameTaskType {
    FIRST_ROUND_LOADING(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                sendMessage.run();
            }, Duration.ofMillis(CardGameState.LOADING.getDuration()));
        }
    },
    FIRST_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startPlay();
                sendMessage.run();
            }, Duration.ofMillis(CardGameState.PLAYING.getDuration()));
        }
    },
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                sendMessage.run();
            }, Duration.ofMillis(CardGameState.SCORE_BOARD.getDuration()));
        }
    },
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                sendMessage.run();
            }, Duration.ofMillis(CardGameState.LOADING.getDuration()));
        }
    },
    SECOND_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startPlay();
                sendMessage.run();
            }, Duration.ofMillis(CardGameState.PLAYING.getDuration()));
        }
    },
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    GAME_FINISH_STATE(CardGameState.DONE, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.changeDoneState();
                MiniGameResult result = cardGame.getResult();
                room.applyMiniGameResult(result);
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    ;

    private final CardGameState state;
    private final CardGameRound round;

    public static CardGameTaskType from(CardGame cardGame) {
        return CardGameTaskType.of(cardGame.getState(), cardGame.getRound());
    }

    public abstract ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage);

    CardGameTaskType(CardGameState state, CardGameRound round) {
        this.state = state;
        this.round = round;
    }

    public static CardGameTaskType of(CardGameState state, CardGameRound round) {
        return Arrays.stream(values()).filter(type -> type.state == state && type.round == round)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드게임 작업입니다."));
    }
}
