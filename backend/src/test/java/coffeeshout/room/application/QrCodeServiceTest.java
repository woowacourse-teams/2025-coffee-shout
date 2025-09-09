package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import coffeeshout.global.config.properties.QrProperties;
import coffeeshout.global.exception.custom.QRCodeGenerationException;
import coffeeshout.global.exception.custom.StorageServiceException;
import coffeeshout.room.domain.RoomErrorCode;
import coffeeshout.room.domain.service.QrCodeGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    QrCodeGenerator qrCodeGenerator;

    @Mock
    StorageService storageService;

    MeterRegistry meterRegistry = new SimpleMeterRegistry();

    QrCodeService qrCodeService;

    String qrCodePrefix = "https://example.com/join";

    @BeforeEach
    void setUp() {
        QrProperties qrProperties = new QrProperties(qrCodePrefix, 10, 100, null, "");
        qrCodeService = new QrCodeService(qrProperties, qrCodeGenerator, storageService, meterRegistry);
    }

    @Test
    @DisplayName("QR 코드 URL 생성이 성공적으로 완료된다")
    void getQrCodeUrl_Success() throws Exception {
        // given
        String contents = "TEST123";
        byte[] qrCodeImage = "mock qr code image".getBytes();
        String storageKey = "qr-code/TEST123/uuid.png";
        String expectedUrl = "https://storage.example.com/qr-code/TEST123/uuid.png";

        when(qrCodeGenerator.generate(anyString())).thenReturn(qrCodeImage);
        when(storageService.upload(contents, qrCodeImage)).thenReturn(storageKey);
        when(storageService.getUrl(storageKey)).thenReturn(expectedUrl);

        // when
        String result = qrCodeService.getQrCodeUrl(contents);

        // then
        assertThat(result).isEqualTo(expectedUrl);

        verify(qrCodeGenerator).generate("https://example.com/join/TEST123");
        verify(storageService).upload(contents, qrCodeImage);
        verify(storageService).getUrl(storageKey);
    }

    @Test
    @DisplayName("QR 코드 생성 실패 시 QR_CODE_GENERATION_FAILED 에러를 던진다")
    void getQrCodeUrl_ThrowsException_WhenQrGenerationFails() throws Exception {
        // given
        String contents = "TEST123";

        when(qrCodeGenerator.generate(anyString()))
                .thenThrow(new RuntimeException("QR code generation failed"));

        // when & then
        assertThatThrownBy(() -> qrCodeService.getQrCodeUrl(contents))
                .isInstanceOf(QRCodeGenerationException.class)
                .hasMessageContaining(RoomErrorCode.QR_CODE_GENERATION_FAILED.getMessage());
    }

    @Test
    @DisplayName("스토리지 업로드 실패 시 해당 예외를 그대로 전파한다 (503 에러)")
    void getQrCodeUrl_PropagatesStorageUploadException() throws Exception {
        // given
        String contents = "TEST123";
        byte[] qrCodeImage = "mock qr code image".getBytes();

        when(qrCodeGenerator.generate(anyString())).thenReturn(qrCodeImage);
        when(storageService.upload(contents, qrCodeImage))
                .thenThrow(new StorageServiceException(RoomErrorCode.QR_CODE_UPLOAD_FAILED, RoomErrorCode.QR_CODE_UPLOAD_FAILED.getMessage()));

        // when & then
        assertThatThrownBy(() -> qrCodeService.getQrCodeUrl(contents))
                .isInstanceOf(StorageServiceException.class)
                .hasMessageContaining(RoomErrorCode.QR_CODE_UPLOAD_FAILED.getMessage());
    }

    @Test
    @DisplayName("URL 생성 실패 시 해당 예외를 그대로 전파한다 (503 에러)")
    void getQrCodeUrl_PropagatesUrlGenerationException() throws Exception {
        // given
        String contents = "TEST123";
        byte[] qrCodeImage = "mock qr code image".getBytes();
        String storageKey = "qr-code/TEST123/uuid.png";

        when(qrCodeGenerator.generate(anyString())).thenReturn(qrCodeImage);
        when(storageService.upload(contents, qrCodeImage)).thenReturn(storageKey);
        when(storageService.getUrl(storageKey))
                .thenThrow(new StorageServiceException(RoomErrorCode.QR_CODE_URL_SIGNING_FAILED,
                        RoomErrorCode.QR_CODE_URL_SIGNING_FAILED.getMessage()));

        // when & then
        assertThatThrownBy(() -> qrCodeService.getQrCodeUrl(contents))
                .isInstanceOf(StorageServiceException.class)
                .hasMessageContaining(RoomErrorCode.QR_CODE_URL_SIGNING_FAILED.getMessage());
    }

    @Test
    @DisplayName("QR 코드 생성 시 올바른 URL을 사용한다")
    void getQrCodeUrl_UsesCorrectUrl() throws Exception {
        // given
        String contents = "ABC123";
        byte[] qrCodeImage = "mock qr code image".getBytes();
        String storageKey = "qr-code/ABC123/uuid.png";
        String expectedUrl = "https://storage.example.com/qr-code/ABC123/uuid.png";

        when(qrCodeGenerator.generate(anyString())).thenReturn(qrCodeImage);
        when(storageService.upload(contents, qrCodeImage)).thenReturn(storageKey);
        when(storageService.getUrl(storageKey)).thenReturn(expectedUrl);

        // when
        qrCodeService.getQrCodeUrl(contents);

        // then
        verify(qrCodeGenerator).generate("https://example.com/join/ABC123");
    }

    @Test
    @DisplayName("일반 예외 발생 시 QR_CODE_GENERATION_FAILED 에러로 래핑한다")
    void getQrCodeUrl_WrapsGenericException() throws Exception {
        // given
        String contents = "TEST123";

        when(qrCodeGenerator.generate(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThatThrownBy(() -> qrCodeService.getQrCodeUrl(contents))
                .isInstanceOf(QRCodeGenerationException.class)
                .hasMessageContaining(RoomErrorCode.QR_CODE_GENERATION_FAILED.getMessage());
    }
}
