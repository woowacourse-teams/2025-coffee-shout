package coffeeshout.global.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamBroadcastService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:app}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;


    public static final String BROADCAST_STREAM = "room:broadcast";
    public static final String RESPONSE_STREAM = "room:broadcast:responses";


    /**
     * 각 인스턴스의 처리 완료 후 응답 전송
     */
    public void sendBroadcastResponse(String requestId, Object response) {
        try {
            stringRedisTemplate.opsForStream().add(
                    RESPONSE_STREAM,
                    Map.of(
                            "requestId", requestId,
                            "response", objectMapper.writeValueAsString(response),
                            "instanceId", getInstanceId(),
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            log.debug("Broadcast response sent: requestId={}, instanceId={}", requestId, getInstanceId());

        } catch (Exception e) {
            log.error("Failed to send broadcast response: requestId={}", requestId, e);
        }
    }


    private String getInstanceId() {
        return applicationName + "-" + serverPort;
    }
}
