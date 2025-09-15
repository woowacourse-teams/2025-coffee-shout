package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.common.task.ChainedTask;
import coffeeshout.minigame.domain.dto.CardGameStateChangeEvent;
import coffeeshout.minigame.domain.dto.MiniGameCompletedLocalEvent;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.Room;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

@Getter
public enum CardGameTaskType {
    FIRST_ROUND_LOADING(CardGameState.FIRST_LOADING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    FIRST_ROUND_DESCRIPTION(CardGameState.PREPARE, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.updateDescription();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    FIRST_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.startPlay();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.startRound();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    SECOND_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.startPlay();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.assignRandomCardsToUnselectedPlayers();
                cardGame.changeScoreBoardState();
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
            }, getState().getDurationMillis());
        }
    },
    GAME_FINISH_STATE(CardGameState.DONE, CardGameRound.SECOND) {
        @Override
        public ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher) {
            return new ChainedTask(() -> {
                cardGame.changeDoneState();
                MiniGameResult result = cardGame.getResult();
                room.applyMiniGameResult(result);
                
                // 카드게임 상태 변경 이벤트 발행
                eventPublisher.publishEvent(new CardGameStateChangeEvent(room.getJoinCode(), cardGame));
                
                // 🔥 미니게임 완료 이벤트 발행 (Redis 동기화용)
                eventPublisher.publishEvent(new MiniGameCompletedLocalEvent(
                    room.getJoinCode(),
                    cardGame.getMiniGameType(),
                    result
                ));
            }, getState().getDurationMillis());
        }
    },
    ;

    private final CardGameState state;
    private final CardGameRound round;

    public static CardGameTaskType from(CardGame cardGame) {
        return CardGameTaskType.of(cardGame.getState(), cardGame.getRound());
    }

    public abstract ChainedTask createTask(CardGame cardGame, Room room, ApplicationEventPublisher eventPublisher);

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
