package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.room.domain.Room;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

@Getter
public enum CardGameTaskType {

    FIRST_ROUND_LOADING(CardGameState.FIRST_LOADING, CardGameRound.FIRST) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.startRound();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    FIRST_ROUND_DESCRIPTION(CardGameState.PREPARE, CardGameRound.FIRST) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.updateDescription();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    FIRST_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.startPlay();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.SECOND) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.startRound();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    SECOND_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.startPlay();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    GAME_FINISH_STATE(CardGameState.DONE, CardGameRound.SECOND) {
        @Override
        public Runnable createTask(
                CardGame cardGame,
                Room room,
                ApplicationEventPublisher eventPublisher
        ) {
            return new MiniGameTask(() -> {
                cardGame.changeDoneState();
                MiniGameResult result = cardGame.getResult();
                room.applyMiniGameResult(result);
                eventPublisher.publishEvent(new CardGameStateChangedEvent(room, cardGame, this));
            }, generateCorrelationId(room, this));
        }
    },
    ;

    private final CardGameState state;
    private final CardGameRound round;

    public static CardGameTaskType from(CardGame cardGame) {
        return of(cardGame.getState(), cardGame.getRound());
    }

    public static CardGameTaskType getFirstTask() {
        return values()[0];
    }

    public abstract Runnable createTask(
            CardGame cardGame,
            Room room,
            ApplicationEventPublisher eventPublisher
    );

    CardGameTaskType(CardGameState state, CardGameRound round) {
        this.state = state;
        this.round = round;
    }

    public static CardGameTaskType of(CardGameState state, CardGameRound round) {
        return Arrays.stream(values()).filter(type -> type.state == state && type.round == round)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드게임 작업입니다."));
    }

    public boolean isLastTask() {
        return this.ordinal() == values().length - 1;
    }

    public CardGameTaskType nextTask() {
        if (isLastTask()) {
            throw new IllegalArgumentException("마지막 작업입니다.");
        }
        return values()[this.ordinal() + 1];
    }

    private static String generateCorrelationId(Room room, CardGameTaskType task) {
        return String.format("JoinCode: %s / gameState: %s", room.getJoinCode().getValue(), task.name());
    }
}
