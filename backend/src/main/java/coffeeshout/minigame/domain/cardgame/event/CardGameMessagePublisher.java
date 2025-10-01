package coffeeshout.minigame.domain.cardgame.event;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartMessage;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangeMessage;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/*
    TODO: redis도입시 event발송에서 redis pub/sub message 발송으로 변경하기
 */

@Component
public class CardGameMessagePublisher {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final ApplicationEventPublisher publisher;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    public CardGameMessagePublisher(ApplicationEventPublisher publisher, LoggingSimpMessagingTemplate messagingTemplate) {
        this.publisher = publisher;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void publishCardGameStateChanged(CardGameStateChangedEvent cardGameStateChangedEvent) {
        final Room room = cardGameStateChangedEvent.room();
        final CardGame cardGame = cardGameStateChangedEvent.cardGame();
        CardGameTaskType cardGameTaskType = cardGameStateChangedEvent.currentTask();
        
        // 비즈니스 로직: 다음 태스크 스케줄링
        publisher.publishEvent(new CardGameStateChangeMessage(
                room.getJoinCode().getValue(),
                cardGameTaskType.name(),
                System.currentTimeMillis() + cardGameTaskType.getState().getDurationMillis().toMillis()
        ));
        
        // 웹소켓 브로드캐스트
        sendCardGameState(cardGame, room.getJoinCode());
    }

    @EventListener
    public void publishCardGameStarted(CardGameStartedEvent cardGameStartedEvent) {
        JoinCode joinCode = cardGameStartedEvent.joinCode();
        CardGame cardGame = cardGameStartedEvent.cardGame();
        
        // 비즈니스 로직: 첫 번째 태스크 스케줄링
        publisher.publishEvent(new CardGameStartMessage(
                joinCode.getValue(),
                CardGameTaskType.getFirstTask().name()
        ));
        
        // 웹소켓 브로드캐스트
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode.getValue()),
                WebSocketResponse.success(new MiniGameStartMessage(cardGame.getMiniGameType()))
        );
    }

    @EventListener
    public void publishCardSelected(CardSelectedEvent cardSelectedEvent) {
        // 웹소켓 브로드캐스트만
        sendCardGameState(cardSelectedEvent.cardGame(), cardSelectedEvent.joinCode());
    }

    private void sendCardGameState(CardGame cardGame, JoinCode joinCode) {
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }
}
