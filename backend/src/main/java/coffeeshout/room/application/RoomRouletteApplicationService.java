package coffeeshout.room.application;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.domain.service.RouletteCommandService;
import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomRouletteApplicationService {

    private final RouletteCommandService rouletteCommandService;

    public Winner spinRoulette(String joinCode, String hostName) {
        return rouletteCommandService.spinRoulette(joinCode, hostName);
    }

    public Room showRoulette(String joinCode) {
        return rouletteCommandService.showRoulette(joinCode);
    }

    public List<ProbabilityResponse> getProbabilities(String joinCode) {
        return rouletteCommandService.getProbabilities(joinCode);
    }
}
