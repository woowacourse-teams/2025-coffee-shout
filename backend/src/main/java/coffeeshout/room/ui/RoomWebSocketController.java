package coffeeshout.room.ui;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomEventPublisher;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import coffeeshout.room.ui.request.MiniGameSelectMessage;
import coffeeshout.room.ui.request.ReadyChangeMessage;
import coffeeshout.room.ui.request.RouletteSpinMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final RoomEventPublisher roomEventPublisher;
    private final RoomService roomService;

    @MessageMapping("/room/{joinCode}/update-players")
    public void broadcastPlayers(@DestinationVariable String joinCode) {
        final PlayerListUpdateEvent event = new PlayerListUpdateEvent(joinCode);
        roomEventPublisher.publish(event);
    }

    @MessageMapping("/room/{joinCode}/update-ready")
    public void broadcastReady(@DestinationVariable String joinCode, ReadyChangeMessage message) {
        final PlayerReadyEvent event = new PlayerReadyEvent(joinCode, message.playerName(), message.isReady());
        roomEventPublisher.publish(event);
    }

    @MessageMapping("/room/{joinCode}/update-minigames")
    public void broadcastMiniGames(@DestinationVariable String joinCode, MiniGameSelectMessage message) {
        final MiniGameSelectEvent event = new MiniGameSelectEvent(joinCode, message.hostName(),
                message.miniGameTypes());
        roomEventPublisher.publish(event);
    }

    @MessageMapping("/room/{joinCode}/show-roulette")
    public void broadcastShowRoulette(@DestinationVariable String joinCode) {
        final RouletteShowEvent event = new RouletteShowEvent(joinCode);
        roomEventPublisher.publish(event);
    }

    @MessageMapping("/room/{joinCode}/spin-roulette")
    public void broadcastRouletteSpin(@DestinationVariable String joinCode, RouletteSpinMessage message) {
        final Room room = roomService.getRoomByJoinCode(joinCode);
        final Winner winner = room.spinRoulette(room.getHost(), new Roulette(new RoulettePicker()));
        final RouletteSpinEvent event = new RouletteSpinEvent(joinCode, message.hostName(), winner);
        roomEventPublisher.publish(event);
    }
}
