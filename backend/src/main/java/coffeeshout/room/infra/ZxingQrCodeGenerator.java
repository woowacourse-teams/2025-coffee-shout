package coffeeshout.room.infra;

import coffeeshout.config.properties.QrProperties;
import coffeeshout.room.domain.service.QrCodeGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ZxingQrCodeGenerator implements QrCodeGenerator {

    private static final String IMAGE_FORMAT = "PNG";

    private final int height;
    private final int width;
    private final QRCodeWriter qrCodeWriter;

    public ZxingQrCodeGenerator(
            QrProperties qrProperties,
            QRCodeWriter qrCodeWriter
    ) {
        this.height = qrProperties.height();
        this.width = qrProperties.width();
        this.qrCodeWriter = qrCodeWriter;
    }

    @Override
    public byte[] generate(String contents) throws IOException {
        try {
            final BitMatrix bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, width, height);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

            return outputStream.toByteArray();
        } catch (WriterException e) {
            log.error("QR코드 생성 중 에러 발생: {}", e.getMessage());
            throw new IOException(e);
        }
    }
}
