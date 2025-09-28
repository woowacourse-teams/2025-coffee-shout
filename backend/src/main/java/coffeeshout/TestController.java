package coffeeshout;

import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestController {

    private final LoggingSimpMessagingTemplate loggingSimpMessagingTemplate;

    record Data(int x, int y, int width, int height) {
    }

    private final List<Integer> locations = new ArrayList<>() {{
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
        this.add(0);
    }};

    final int startX = 0;
    final int endX = 1000;

    /*
        1. 이동 해야할 총 거리와 정해진 시간이 있다. ex) 1000px 4초

     */
    @Scheduled(fixedRate = 5)
    void process() throws InterruptedException {
        List<Data> response = generate();
        IntStream.range(0, 9).forEach(i -> loggingSimpMessagingTemplate.convertAndSend("/topic/test", response));
    }

    private List<Data> generate() {
        final List<Data> ret = new ArrayList<>();
        for (int i = 0; i < locations.size(); ++i) {
            int delta = i * 3 + 1;
            int location = locations.get(i);
            int nextLocation = location + delta >= endX ?startX : location + delta;
            locations.set(i, nextLocation);
            ret.add(new Data(nextLocation, i * 50, 30, 30));
        }
        return ret;
    }
}
