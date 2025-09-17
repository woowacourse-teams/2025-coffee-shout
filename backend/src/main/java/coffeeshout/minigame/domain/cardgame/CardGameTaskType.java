package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.common.task.ChainedTask;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.domain.cardgame.service.CardGameQueryService;
import coffeeshout.minigame.domain.dto.CardGameStateChangeEvent;
import coffeeshout.minigame.domain.dto.MiniGameCompletedEvent;
import coffeeshout.room.domain.JoinCode;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

@Getter
public enum CardGameTaskType {
    FIRST_ROUND_LOADING(CardGameState.FIRST_LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.startRound();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    FIRST_ROUND_DESCRIPTION(CardGameState.PREPARE, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.updateDescription();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    FIRST_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.startPlay();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.assignRandomCardsToUnselectedPlayers();
                        cardGame.changeScoreBoardState();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.startRound();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    SECOND_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.startPlay();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.assignRandomCardsToUnselectedPlayers();
                        cardGame.changeScoreBoardState();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    GAME_FINISH_STATE(CardGameState.DONE, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(
                JoinCode joinCode,
                ApplicationEventPublisher eventPublisher,
                CardGameQueryService queryService,
                CardGameCommandService commandService
        ) {
            return new ChainedTask(
                    () -> {
                        final CardGame cardGame = queryService.getByJoinCode(joinCode);
                        cardGame.changeDoneState();
                        commandService.save(cardGame);
                        eventPublisher.publishEvent(new MiniGameCompletedEvent(
                                cardGame.getJoinCode(),
                                cardGame.getResult()
                        ));
                        eventPublisher.publishEvent(new CardGameStateChangeEvent(cardGame.getJoinCode(), cardGame));
                    }, getState().getDurationMillis()
            );
        }
    },
    ;

    private final CardGameState state;
    private final CardGameRound round;

    public static CardGameTaskType from(CardGame cardGame) {
        return CardGameTaskType.of(cardGame.getState(), cardGame.getRound());
    }

    public abstract ChainedTask createTask(
            JoinCode joinCode,
            ApplicationEventPublisher eventPublisher,
            CardGameQueryService queryService,
            CardGameCommandService commandService
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
}
