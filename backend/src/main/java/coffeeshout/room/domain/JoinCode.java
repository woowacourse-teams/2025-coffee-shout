package coffeeshout.room.domain;

import static org.springframework.util.Assert.state;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record JoinCode(
        String value
) {

    public String getValue() {
        return value;
    }

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 5;

    public JoinCode {
        state(value.length() == CODE_LENGTH, "코드는 5자리여야 합니다.");
        value.chars().forEach(charCode -> state(isValidCharacter(charCode), "허용되지 않는 문자가 포함되어 있습니다."));
    }

    public static JoinCode generate() {
        final List<Integer> asciiCodes = convertAsciiList();
        Collections.shuffle(asciiCodes);
        return new JoinCode(asciiCodes.stream()
                .limit(CODE_LENGTH)
                .map(JoinCode::convertAsciiToString)
                .collect(Collectors.joining()));
    }

    private static List<Integer> convertAsciiList() {
        return JoinCode.CHARSET.chars().boxed().collect(Collectors.toList());
    }

    private static String convertAsciiToString(int asciiCode) {
        return String.valueOf((char) asciiCode);
    }

    private boolean isValidCharacter(int charCode) {
        return CHARSET.indexOf(charCode) > -1;
    }
}
