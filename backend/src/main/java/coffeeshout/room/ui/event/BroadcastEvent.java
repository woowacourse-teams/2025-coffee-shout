package coffeeshout.room.ui.event;

public interface BroadcastEvent {
    String joinCode();
    BroadcastEventType getBroadcastEventType();
}
