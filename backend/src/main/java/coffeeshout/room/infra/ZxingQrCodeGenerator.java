package coffeeshout.room.infra;

import coffeeshout.room.domain.service.QrCodeGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ZxingQrCodeGenerator implements QrCodeGenerator {

    private static final String IMAGE_FORMAT = "PNG";

    private final int height;
    private final int width;
    private final QRCodeWriter qrCodeWriter;

    public ZxingQrCodeGenerator(
            @Value("${room.qr.height}") int height,
            @Value("${room.qr.width}") int width,
            QRCodeWriter qrCodeWriter
    ) {
        this.height = height;
        this.width = width;
        this.qrCodeWriter = qrCodeWriter;
    }

    @Override
    public byte[] generate(String url) throws WriterException, IOException {
        final BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

        return outputStream.toByteArray();
    }
}
