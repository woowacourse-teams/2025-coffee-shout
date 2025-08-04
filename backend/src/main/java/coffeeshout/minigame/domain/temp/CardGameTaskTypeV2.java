package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.room.domain.Room;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum CardGameTaskTypeV2 {
    FIRST_ROUND_LOADING(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.startRound();
                        sendMessage.run();
                    }).delayAfter(getState().getDurationMillis())
                    .build();
        }
    },
    FIRST_ROUND_PLAYING_COMBINED(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            ChainedTaskV2 delayTask = ChainedTaskV2.builder()
                    .action(cardGame::assignRandomCardsToUnselectedPlayers)
                    .build();
            
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.startPlay();
                        sendMessage.run();
                    })
                    .delayAfter(getState().getDurationMillis())
                    .chainOnCondition(cardGame::isFinishedThisRound, delayTask)
                    .build();
        }
    },
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.changeScoreBoardState();
                        sendMessage.run();
                    })
                    .delayAfter(getState().getDurationMillis())
                    .build();
        }
    },
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.SECOND) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.startRound();
                        sendMessage.run();
                    }).delayAfter(getState().getDurationMillis())
                    .build();
        }
    },
    SECOND_ROUND_PLAYING_STATE(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.startPlay();
                        sendMessage.run();
                    })
                    .build();
        }
    },
    SECOND_ROUND_PLAYING_DELAY(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .delayBefore(getState().getDurationMillis())
                    .action(cardGame::assignRandomCardsToUnselectedPlayers)
                    .build();
        }
    },
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.changeScoreBoardState();
                        sendMessage.run();
                    })
                    .delayAfter(getState().getDurationMillis())
                    .build();
        }
    },
    GAME_FINISH_STATE(CardGameState.DONE, CardGameRound.SECOND) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.changeDoneState();
                        MiniGameResult result = cardGame.getResult();
                        room.applyMiniGameResult(result);
                        sendMessage.run();
                    })
                    .build();
        }
    },
    ;

    private final CardGameState state;
    private final CardGameRound round;

    public static CardGameTaskTypeV2 from(CardGame cardGame) {
        return CardGameTaskTypeV2.of(cardGame.getState(), cardGame.getRound());
    }

    public abstract ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage);

    CardGameTaskTypeV2(CardGameState state, CardGameRound round) {
        this.state = state;
        this.round = round;
    }

    public static CardGameTaskTypeV2 of(CardGameState state, CardGameRound round) {
        return Arrays.stream(values()).filter(type -> type.state == state && type.round == round)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드게임 작업입니다."));
    }

    public static List<ChainedTaskV2> getAllTasks(CardGame cardGame, Room room, Runnable message) {
        return Arrays.stream(values()).map(type -> type.createTask(cardGame, room, message))
                .toList();
    }
}
