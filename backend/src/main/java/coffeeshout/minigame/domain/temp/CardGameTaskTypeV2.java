package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.room.domain.Room;
import lombok.Getter;

@Getter
public enum CardGameTaskTypeV2 {
    FIRST_ROUND_LOADING(CardGameState.LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.changeLoadingState();
                        sendMessage.run();
                    }).delayAfter(getState().getDurationMillis())
                    .build();
        }
    },
    FIRST_ROUND_PLAYING_STATE(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .action(() -> {
                        cardGame.changeLoadingState();
                        sendMessage.run();
                    })
                    .build();
        }
    },
    FIRST_ROUND_PLAYING_DELAY(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage) {
            return ChainedTaskV2.builder()
                    .delayBefore(getState().getDurationMillis())
                    .action(cardGame::assignRandomCardsToUnselectedPlayers)
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
                        cardGame.changeLoadingState();
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
                        cardGame.changeLoadingState();
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

    public abstract ChainedTaskV2 createTask(CardGame cardGame, Room room, Runnable sendMessage);

    CardGameTaskTypeV2(CardGameState state, CardGameRound round) {
        this.state = state;
        this.round = round;
    }
}
