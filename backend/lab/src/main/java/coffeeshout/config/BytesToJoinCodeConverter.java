package coffeeshout.config;

import coffeeshout.domain.JoinCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@ReadingConverter
@Component
public class BytesToJoinCodeConverter implements Converter<byte[], JoinCode> {

    @Override
    public JoinCode convert(byte[] source) {
        return new JoinCode(new String(source));
    }
}
