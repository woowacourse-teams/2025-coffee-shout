package coffeeshout.minigame.cardgame.domain.cardgame.event;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGame;
import coffeeshout.minigame.cardgame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStateChangeMessage;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.CardSelectedEvent;
import coffeeshout.minigame.cardgame.domain.cardgame.event.dto.MiniGameStartedEvent;
import coffeeshout.minigame.cardgame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.cardgame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import generator.annotaions.MessageResponse;
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
    @MessageResponse(
            path = "/room/{joinCode}/round",
            returnType = MiniGameStartMessage.class
    )
    public void publishCardGameStarted(MiniGameStartedEvent miniGameStartedEvent) {
        // 웹소켓 브로드캐스트
        final MiniGameType miniGameType = MiniGameType.valueOf(miniGameStartedEvent.gameType());
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, miniGameStartedEvent.joinCode()),
                WebSocketResponse.success(new MiniGameStartMessage(miniGameType))
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
