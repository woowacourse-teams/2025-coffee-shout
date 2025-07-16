package coffeeshout.coffeeshout.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record JoinCode(String address) {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final List<String> CHARACTERS = getCharacters();
    private static final int CODE_LENGTH = 5;

    public JoinCode {
        validate(address);
    }

    private static List<String> getCharacters() {
        return Arrays.stream(CHARSET.split("")).toList();
    }

    public static JoinCode generate() {
        Collections.shuffle(CHARACTERS);
        String code = String.join("", CHARACTERS.subList(0, CODE_LENGTH));
        return new JoinCode(code);
    }

    public void validate(String address) {
        if (address.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("코드는 5자리여야 합니다.");
        }

        for (char c : address.toCharArray()) {
            validateInvalidChar(c);
        }
    }

    private void validateInvalidChar(char c) {
        if (CHARSET.indexOf(c) == -1) {
            throw new IllegalArgumentException("허용되지 않는 문자가 포함되어 있습니다.");
        }
    }
}
