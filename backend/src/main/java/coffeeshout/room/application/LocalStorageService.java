package coffeeshout.room.application;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"local", "test"})
@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private final MeterRegistry meterRegistry;

    public LocalStorageService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public String uploadDataAndGetUrl(String contents, byte[] data) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(data);
            String dataUrl = "data:image/png;base64," + base64Image;

            log.info("로컬 환경에서 Data URL 생성 완료: contents={}, length={}", contents, dataUrl.length());
            meterRegistry.counter("qr.local.data.url.success", "contents", contents).increment();

            return dataUrl;
        } catch (Exception e) {
            meterRegistry.counter("qr.local.data.url.failed",
                    "contents", contents,
                    "error", e.getClass().getSimpleName()).increment();
            log.error("로컬 Data URL 생성 실패: contents={}, error={}", contents, e.getMessage(), e);
            throw new RuntimeException("로컬 QR 코드 Data URL 생성에 실패했습니다.", e);
        }
    }
}
