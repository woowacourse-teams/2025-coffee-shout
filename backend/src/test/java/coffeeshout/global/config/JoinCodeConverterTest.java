package coffeeshout.global.config;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.global.config.JoinCodeConverter.JoinCodeToStringConverter;
import coffeeshout.global.config.JoinCodeConverter.StringToJoinCodeConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JoinCodeConverterTest {

    private final JoinCodeToStringConverter joinCodeToStringConverter = new JoinCodeToStringConverter();
    private final StringToJoinCodeConverter stringToJoinCodeConverter = new StringToJoinCodeConverter();

    @Test
    void JoinCode를_String으로_변환할_수_있다() {
        // Given
        JoinCode joinCode = new JoinCode("ABCDE");

        // When
        String result = joinCodeToStringConverter.convert(joinCode);

        // Then
        assertThat(result).isEqualTo("ABCDE");
    }

    @Test
    void String을_JoinCode로_변환할_수_있다() {
        // Given
        String joinCodeString = "FGHJK";

        // When
        JoinCode result = stringToJoinCodeConverter.convert(joinCodeString);

        // Then
        assertThat(result.getValue()).isEqualTo("FGHJK");
    }
}