package coffeeshout;

import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import java.util.List;
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

    final int startX = 0;
    final int endX = 1000;
    final int deltaX = 30;
    int currX = startX;

    /*
        1. 이동 해야할 총 거리와 정해진 시간이 있다. ex) 1000px 4초

     */
    @Scheduled(fixedRate = 5)
    void process() throws InterruptedException {
        List<Data> response = generate(currX);
        IntStream.range(0, 9).forEach(i -> loggingSimpMessagingTemplate.convertAndSend("/topic/test", response));
        currX += currX >= endX ? -currX : deltaX;
    }

    private List<Data> generate(int x) {
        return IntStream.range(0, 9).mapToObj(i -> new Data(x, i * 50, 30, 30)).toList();
    }
}
