package coffeeshout.config;

import coffeeshout.domain.Player;
import coffeeshout.domain.PlayerName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@ReadingConverter
@Component
public class BytesToPlayerConverter implements Converter<byte[], Player> {

    @Override
    public Player convert(byte[] source) {
        return new Player(new PlayerName(new String(source)));
    }
}
