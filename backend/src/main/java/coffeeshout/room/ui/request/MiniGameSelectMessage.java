package coffeeshout.room.ui.request;

import coffeeshout.room.domain.MiniGameType;

public record MiniGameSelectMessage(
        String hostName,
        MiniGameType miniGameType
) {

}
