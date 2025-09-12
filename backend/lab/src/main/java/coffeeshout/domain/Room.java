package coffeeshout.domain;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RequiredArgsConstructor
@RedisHash("room")
public class Room {

    @Id
    private final String joinCode;
    private final List<Player> host;
    private final RoomState roomState;
}
