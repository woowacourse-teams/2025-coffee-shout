package coffeeshout.room.ui.response;

import coffeeshout.room.domain.QrCodeStatus;

public record QrCodeStatusResponse(
        QrCodeStatus status,
        String url
) {
}
