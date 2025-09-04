package coffeeshout.room.application;

import coffeeshout.global.exception.GlobalErrorCode;
import coffeeshout.global.exception.custom.QRCodeGenerationException;
import coffeeshout.room.domain.service.QrCodeGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QrCodeService {

    private final String qrCodePrefix;
    private final QrCodeGenerator qrCodeGenerator;
    private final StorageService storageService;
    private final MeterRegistry meterRegistry;
    private final Timer qrCodeGenerationTimer;

    public QrCodeService(@Value("${room.qr.prefix}") String qrCodePrefix,
                         QrCodeGenerator qrCodeGenerator,
                         StorageService storageService,
                         MeterRegistry meterRegistry) {
        this.qrCodePrefix = qrCodePrefix;
        this.qrCodeGenerator = qrCodeGenerator;
        this.storageService = storageService;
        this.meterRegistry = meterRegistry;
        this.qrCodeGenerationTimer = Timer.builder("qr.code.generation.time")
                .description("Time taken to generate QR code")
                .register(meterRegistry);
    }

    @Retryable(retryFor = Exception.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String getQrCodeUrl(String contents) {
        try {
            byte[] qrCodeImage = generateQrCode(contents);
            return storageService.uploadDataAndGetUrl(contents, qrCodeImage);
        } catch (Exception e) {
            meterRegistry.counter("qr.image.url.generation.failed",
                    "contents", contents,
                    "error", e.getClass().getSimpleName()).increment();
            log.error("QR 이미지 URL 생성 실패: contents={}, error={}", contents, e.getMessage(), e);
            throw new QRCodeGenerationException(GlobalErrorCode.QR_CODE_GENERATION_FAILED, "QR 이미지 URL 생성에 실패했습니다.");
        }
    }

    private byte[] generateQrCode(String contents) {
        try {
            return qrCodeGenerationTimer.recordCallable(() ->
                    qrCodeGenerator.generate(qrCodePrefix + contents)
            );
        } catch (Exception e) {
            meterRegistry.counter("qr.generation.failed",
                    "JoinCode", contents,
                    "error", e.getClass().getSimpleName()).increment();
            log.error("QR 코드 생성 실패: contents={}, error={}", contents, e.getMessage(), e);
            throw new QRCodeGenerationException(GlobalErrorCode.QR_CODE_GENERATION_FAILED, "QR 코드 생성에 실패했습니다.");
        }
    }
}
