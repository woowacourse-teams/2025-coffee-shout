package coffeeshout.room.application;

import coffeeshout.room.domain.service.RouletteCommandService;
import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomRouletteApplicationService {

    private final RouletteCommandService rouletteCommandService;

    public List<ProbabilityResponse> getProbabilities(String joinCode) {
        return rouletteCommandService.getProbabilities(joinCode);
    }
}
