package coffeeshout.domain;

import java.util.List;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash("room")
public class Room {

    @Id
    private final JoinCode joinCode;
    private final List<Player> host;
    private final RoomState roomState;
    private final SomeInterface someInterface;

    public Room(
            JoinCode joinCode,
            List<Player> host,
            RoomState roomState,
            SomeInterface someInterface
    ) {
        this.joinCode = joinCode;
        this.host = host;
        this.roomState = roomState;
        this.someInterface = someInterface;
    }
}
