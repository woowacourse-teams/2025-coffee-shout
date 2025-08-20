package coffeeshout.global.websocket.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomStateUpdateEventListenerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private LoggingSimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RoomStateUpdateEventListener listener;

    @Test
    void 방이_존재할_때는_상태_브로드캐스트를_수행한다() {
        // given
        String joinCode = "TEST3";
        RoomStateUpdateEvent event = new RoomStateUpdateEvent(joinCode, "test reason");

        when(roomService.roomExists(joinCode)).thenReturn(true);
        when(roomService.getAllPlayers(joinCode)).thenReturn(List.of());
        when(roomService.getProbabilities(joinCode)).thenReturn(Map.of());

        // when
        listener.handleRoomStateUpdate(event);

        // then
        verify(roomService).roomExists(joinCode);
        verify(roomService).getAllPlayers(joinCode);
        verify(roomService).getProbabilities(joinCode);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any());
    }

    @Test
    void 방이_존재하지_않을_때는_상태_브로드캐스트를_수행하지_않는다() {
        // given
        String joinCode = "ERROR";
        RoomStateUpdateEvent event = new RoomStateUpdateEvent(joinCode, "test reason");

        when(roomService.roomExists(joinCode)).thenReturn(false);

        // when
        listener.handleRoomStateUpdate(event);

        // then
        verify(roomService).roomExists(joinCode);
        verify(roomService, never()).getAllPlayers(anyString());
        verify(roomService, never()).getProbabilities(anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any());
    }
}
