package coffeeshout.config;

import coffeeshout.domain.Player;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@WritingConverter
@Component
public class PlayerToBytesConverter implements Converter<Player, byte[]> {

    @Override
    public byte[] convert(Player source) {
        return source.getName().value().getBytes();
    }
}
