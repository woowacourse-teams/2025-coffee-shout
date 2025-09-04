package coffeeshout.room.application;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@Profile("!local & !test")
public class S3Service implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignedUrlExpirationHours;
    private final MeterRegistry meterRegistry;
    private final Timer s3UploadTimer;

    public S3Service(S3Client s3Client,
                     S3Presigner s3Presigner,
                     @Value("${spring.cloud.aws.s3.bucket}") String bucketName,
                     @Value("${room.qr.presignedUrl.expirationHours}") int presignedUrlExpirationHours,
                     MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedUrlExpirationHours = presignedUrlExpirationHours;
        this.meterRegistry = meterRegistry;
        this.s3UploadTimer = Timer.builder("s3.upload.time")
                .description("Time taken to upload QR code to S3")
                .register(meterRegistry);
    }

    @Override
    public String uploadDataAndGetUrl(String contents, byte[] data) {
        try {
            String s3Key = uploadQrCodeToS3(contents, data);
            return generatePresignedUrl(s3Key);
        } catch (Exception e) {
            meterRegistry.counter("s3.qr.upload.failed",
                    "contents", contents,
                    "error", e.getClass().getSimpleName()).increment();
            log.error("S3 QR 코드 업로드 및 URL 생성 실패: contents={}, error={}", contents, e.getMessage(), e);
            throw new RuntimeException("S3 QR 코드 업로드에 실패했습니다.", e);
        }
    }

    @Retryable(retryFor = Exception.class, backoff = @Backoff(delay = 500, multiplier = 2))
    private String uploadQrCodeToS3(String contents, byte[] qrCodeImage) throws Exception {
        return s3UploadTimer.recordCallable(() -> {
            String s3Key = "qr-code/" + contents + "/" + UUID.randomUUID() + ".png";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("image/png")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(qrCodeImage));
            log.info("QR 코드 S3 업로드 완료: contents={}, s3Key={}", contents, s3Key);

            meterRegistry.counter("s3.upload.success", "contents", contents).increment();
            return s3Key;
        });
    }

    private String generatePresignedUrl(String s3Key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(presignedUrlExpirationHours))
                .getObjectRequest(b -> b
                        .bucket(bucketName)
                        .key(s3Key)
                )
                .build();

        URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();
        log.info("Presigned URL 생성 완료: s3Key={}, url={}", s3Key, presignedUrl.toString());

        return presignedUrl.toString();
    }
}
