package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.player.ColorUsage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryColorUsage {

    private final Map<String, ColorUsage> roomColorUsages;

    public MemoryColorUsage() {
        this.roomColorUsages = new ConcurrentHashMap<>();
    }

    public void put(String value, ColorUsage colorUsage) {
        roomColorUsages.put(value, colorUsage);
    }

    public ColorUsage get(String joinCode) {
        return roomColorUsages.get(joinCode);
    }

    public void remove(String joinCode) {
        roomColorUsages.remove(joinCode);
    }
}
