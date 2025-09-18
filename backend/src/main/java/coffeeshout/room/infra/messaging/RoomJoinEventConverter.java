package coffeeshout.room.infra.messaging;

import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RoomJoinEvent와 Redis Stream 간의 변환을 담당하는 컨버터 직렬화/역직렬화 로직을 중앙에서 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomJoinEventConverter {

    private final ObjectMapper objectMapper;

    /**
     * RoomJoinEvent를 Redis Stream용 플랫 Map으로 변환
     */
    public Map<String, String> toFlatMap(RoomJoinEvent event) {
        try {
            return Map.of(
                    "eventId", event.eventId(),
                    "eventType", event.eventType().name(),
                    "joinCode", event.joinCode(),
                    "guestName", event.guestName(),
                    "selectedMenuRequest", objectMapper.writeValueAsString(event.selectedMenuRequest()),
                    "timestamp", event.timestamp().toString()
            );
        } catch (Exception e) {
            log.error("Failed to convert RoomJoinEvent to flat map: {}", event, e);
            throw new RuntimeException("Failed to serialize RoomJoinEvent", e);
        }
    }

    /**
     * Redis Stream의 플랫 Map을 RoomJoinEvent로 변환
     */
    public RoomJoinEvent fromFlatMap(Map<String, String> flatMap) {
        try {
            String eventId = getRequiredField(flatMap, "eventId");
            RoomEventType eventType = RoomEventType.valueOf(getRequiredField(flatMap, "eventType"));
            String joinCode = getRequiredField(flatMap, "joinCode");
            String guestName = getRequiredField(flatMap, "guestName");
            String selectedMenuRequestJson = getRequiredField(flatMap, "selectedMenuRequest");
            String timestampStr = getRequiredField(flatMap, "timestamp");

            // JSON 문자열을 SelectedMenuRequest 객체로 변환
            SelectedMenuRequest selectedMenuRequest = objectMapper.readValue(
                    selectedMenuRequestJson,
                    SelectedMenuRequest.class
            );

            // timestamp 문자열을 Instant로 변환
            Instant timestamp = Instant.parse(timestampStr);

            // RoomJoinEvent 재구성
            return new RoomJoinEvent(eventId, eventType, joinCode, guestName, selectedMenuRequest, timestamp);

        } catch (Exception e) {
            log.error("Failed to convert flat map to RoomJoinEvent: {}", flatMap, e);
            throw new RuntimeException("Failed to deserialize RoomJoinEvent", e);
        }
    }

    /**
     * Map에서 필수 필드 추출 (null 체크 포함)
     */
    private String getRequiredField(Map<String, String> map, String fieldName) {
        String value = map.get(fieldName);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value;
    }
}
