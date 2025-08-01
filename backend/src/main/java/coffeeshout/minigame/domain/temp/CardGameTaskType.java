package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.room.domain.Room;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum CardGameTaskType {
    FIRST_ROUND_LOADING_STATE(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.changeLoadingState();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    FIRST_ROUND_LOADING_DELAY(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
            }, Duration.ofMillis(CardGameState.LOADING.getDuration()));
        }
    },
    FIRST_ROUND_PLAYING_STATE(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    FIRST_ROUND_PLAYING_DELAY(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(
                    cardGame::assignRandomCardsToUnselectedPlayers,
                    Duration.ofMillis(CardGameState.PLAYING.getDuration())
            );
        }
    },
    FIRST_ROUND_SCORE_BOARD_STATE(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.changeScoreBoardState();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    FIRST_ROUND_SCORE_BOARD_DELAY(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
            }, Duration.ofMillis(CardGameState.SCORE_BOARD.getDuration()));
        }
    },
    SECOND_ROUND_LOADING_STATE(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.changeLoadingState();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    SECOND_ROUND_LOADING_DELAY(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
            }, Duration.ofMillis(CardGameState.LOADING.getDuration()));
        }
    },
    SECOND_ROUND_PLAYING_STATE(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    SECOND_ROUND_PLAYING_DELAY(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
            }, Duration.ofMillis(CardGameState.PLAYING.getDuration()));
        }
    },
    SECOND_ROUND_SCORE_BOARD_STATE(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
                cardGame.changeScoreBoardState();
                sendMessage.run();
            }, Duration.ofMillis(0));
        }
    },
    SECOND_ROUND_SCORE_BOARD_DELAY(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return new ChainedTask(() -> {
            }, Duration.ofMillis(CardGameState.SCORE_BOARD.getDuration()));
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

    public static List<ChainedTask> getAllTasks(CardGame cardGame, Room room, Runnable message) {
        return Arrays.stream(values()).map(type -> type.createTask(cardGame, room, message))
                .toList();
    }
}
