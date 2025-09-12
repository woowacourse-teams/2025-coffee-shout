package coffeeshout.global.config;

import coffeeshout.room.domain.JoinCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

public class JoinCodeConverter {

    @WritingConverter
    @Component
    public static class JoinCodeToStringConverter implements Converter<JoinCode, String> {
        @Override
        public String convert(JoinCode source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    @Component
    public static class StringToJoinCodeConverter implements Converter<String, JoinCode> {
        @Override
        public JoinCode convert(String source) {
            return new JoinCode(source);
        }
    }
}