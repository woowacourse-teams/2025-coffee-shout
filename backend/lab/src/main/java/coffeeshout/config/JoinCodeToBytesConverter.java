package coffeeshout.config;

import coffeeshout.domain.JoinCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@WritingConverter
@Component
public class JoinCodeToBytesConverter implements Converter<JoinCode, byte[]> {

    @Override
    public byte[] convert(JoinCode source) {
        return source.getValue().getBytes();
    }
}
