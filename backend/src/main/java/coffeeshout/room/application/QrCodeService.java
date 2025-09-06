package coffeeshout.room.application;

import coffeeshout.config.properties.QrProperties;
import coffeeshout.global.exception.custom.QRCodeGenerationException;
import coffeeshout.global.exception.custom.StorageServiceException;
import coffeeshout.room.domain.RoomErrorCode;
import coffeeshout.room.domain.service.QrCodeGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class QrCodeService {

    private final String qrCodePrefix;
    private final QrCodeGenerator qrCodeGenerator;
    private final StorageService storageService;
    private final MeterRegistry meterRegistry;
    private final Timer qrCodeGenerationTimer;

    public QrCodeService(QrProperties qrProperties,
                         QrCodeGenerator qrCodeGenerator,
                         StorageService storageService,
                         MeterRegistry meterRegistry) {
        this.qrCodePrefix = qrProperties.prefix();
        this.qrCodeGenerator = qrCodeGenerator;
        this.storageService = storageService;
        this.meterRegistry = meterRegistry;
        this.qrCodeGenerationTimer = Timer.builder("qr.code.generation.time")
                .description("Time taken to generate QR code")
                .register(meterRegistry);
    }

    public String getQrCodeUrl(String contents) {
        try {
            byte[] qrCodeImage = generateQrCode(contents);
            String storageKey = uploadToStorage(contents, qrCodeImage);
            return getStorageUrl(storageKey);
        } catch (Exception e) {
            meterRegistry.counter("qr.service.failed",
                    "error", e.getClass().getSimpleName()).increment();
            log.error("QR 이미지 URL 생성 실패: contents={}, error={}", contents, e.getMessage(), e);

            throw e;
        }
    }

    @Retryable(retryFor = StorageServiceException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    private String uploadToStorage(String contents, byte[] qrCodeImage) {
        return storageService.upload(contents, qrCodeImage);
    }

    @Retryable(retryFor = StorageServiceException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    private String getStorageUrl(String storageKey) {
        return storageService.getUrl(storageKey);
    }

    private byte[] generateQrCode(String contents) {
        String url = getUrl(contents);

        try {
            return qrCodeGenerationTimer.recordCallable(() ->
                    qrCodeGenerator.generate(url)
            );
        } catch (Exception e) {
            meterRegistry.counter("qr.generation.failed",
                    "error", e.getClass().getSimpleName()).increment();
            log.error("QR 코드 생성 실패: contents={}, error={}", contents, e.getMessage(), e);

            throw new QRCodeGenerationException(RoomErrorCode.QR_CODE_GENERATION_FAILED, "QR 코드 생성에 실패했습니다.", e);
        }
    }

    private String getUrl(String contents) {
        return UriComponentsBuilder.fromUriString(qrCodePrefix)
                .pathSegment(contents)
                .toUriString();
    }
}
