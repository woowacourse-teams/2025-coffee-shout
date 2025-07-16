package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.coffeeshout.domain.room.JoinCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JoinCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {"ABCDE", "A2B2C"})
    void 조인코드는_규칙에_맞게_생성된다(String address) {
        // given
        JoinCode result = new JoinCode(address);
        System.out.println(JoinCode.generate());
        // when & then
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABCDEF", "A1LB2", "#ABCD"})
    void 조인코드가_규칙에_맞지_않는다면_예외를_발생한다(String address) {
        assertThatThrownBy(() -> new JoinCode(address))
                .isInstanceOf(IllegalStateException.class);
    }
}
