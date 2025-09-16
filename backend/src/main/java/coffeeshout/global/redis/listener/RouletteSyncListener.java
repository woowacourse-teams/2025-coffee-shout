package coffeeshout.global.redis.listener;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.event.roulette.RouletteSpinEvent;
import coffeeshout.room.domain.repository.MemoryRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouletteSyncListener implements MessageListener {

    private final MemoryRoomRepository roomRepository;
    private final InstanceConfig instanceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());

            if ("roulette:spin".equals(channel)) {
                handleRouletteSpin(messageBody);
            }
        } catch (Exception e) {
            log.error("Roulette 동기화 메시지 처리 실패: error={}", e.getMessage(), e);
        }
    }

    private void handleRouletteSpin(String messageBody) {
        try {
            RouletteSpinEvent event = objectMapper.readValue(messageBody, RouletteSpinEvent.class);

            if (event.instanceId().equals(instanceConfig.getInstanceId())) {
                return;
            }

            roomRepository.syncRouletteSpin(event.joinCode(), event.winner());

        } catch (Exception e) {
            log.error("룰렛 스핀 이벤트 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
