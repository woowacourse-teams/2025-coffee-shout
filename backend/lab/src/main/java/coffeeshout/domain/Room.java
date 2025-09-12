package coffeeshout.domain;

import java.util.List;
import java.util.Map;
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
    private final Map<Player, RoomState> map;
    private final SomeInterface someInterface;

    public Room(
            JoinCode joinCode,
            List<Player> host,
            RoomState roomState, Map<Player, RoomState> map,
            SomeInterface someInterface
    ) {
        this.joinCode = joinCode;
        this.host = host;
        this.roomState = roomState;
        this.map = map;
//        this.map = map;
        this.someInterface = someInterface;
    }
}
