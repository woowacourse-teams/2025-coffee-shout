package coffeeshout.room.ui.request;

public record RoomEnterRequest(
        String joinCode,
        String guestName,
        Long menuId
) {
}
