package coffeeshout.room.ui.response;

import coffeeshout.room.domain.QrCodeStatus;
import jakarta.annotation.Nullable;

public record QrCodeStatusResponse(
        QrCodeStatus status,
        @Nullable String qrCodeUrl
) {
}
