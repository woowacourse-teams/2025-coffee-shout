package coffeeshout.minigame.domain.cardgame.round;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import coffeeshout.room.domain.JoinCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundManagerRegistryTest {

    @Mock
    private RoundManagerFactory roundManagerFactory;

    @Mock
    private RoomRoundManager mockRoundManager;

    private RoundManagerRegistry registry;
    private JoinCode joinCode;

    @BeforeEach
    void setUp() {
        registry = new RoundManagerRegistry(roundManagerFactory);
        joinCode = new JoinCode("TEST123");
    }

    @Test
    void 처음_요청_시_새로운_RoundManager를_생성한다() {
        // given
        when(roundManagerFactory.create(joinCode)).thenReturn(mockRoundManager);

        // when
        RoomRoundManager result = registry.getOrCreate(joinCode);

        // then
        assertThat(result).isEqualTo(mockRoundManager);
        verify(roundManagerFactory).create(joinCode);
    }

    @Test
    void 동일한_방에_대해_같은_RoundManager를_반환한다() {
        // given
        when(roundManagerFactory.create(joinCode)).thenReturn(mockRoundManager);

        // when
        RoomRoundManager first = registry.getOrCreate(joinCode);
        RoomRoundManager second = registry.getOrCreate(joinCode);

        // then
        assertThat(first).isEqualTo(second);
        assertThat(first).isEqualTo(mockRoundManager);
        
        // 팩토리는 한 번만 호출되어야 함
        verify(roundManagerFactory, times(1)).create(joinCode);
    }

    @Test
    void 존재하지_않는_방에_대해_null을_반환한다() {
        // when
        RoomRoundManager result = registry.get(joinCode);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 방_제거_시_RoundManager_정리가_호출된다() {
        // given
        when(roundManagerFactory.create(joinCode)).thenReturn(mockRoundManager);
        registry.getOrCreate(joinCode);

        // when
        registry.remove(joinCode);

        // then
        verify(mockRoundManager).cleanup();
        assertThat(registry.get(joinCode)).isNull();
    }

    @Test
    void 활성_방_수를_정확히_반환한다() {
        // given
        JoinCode joinCode1 = new JoinCode("TEST1");
        JoinCode joinCode2 = new JoinCode("TEST2");
        
        when(roundManagerFactory.create(any())).thenReturn(mockRoundManager);

        // when
        registry.getOrCreate(joinCode1);
        registry.getOrCreate(joinCode2);

        // then
        assertThat(registry.getActiveRoomCount()).isEqualTo(2);
    }

    @Test
    void 방_존재_여부를_정확히_확인한다() {
        // given
        when(roundManagerFactory.create(joinCode)).thenReturn(mockRoundManager);

        // when
        boolean beforeCreate = registry.exists(joinCode);
        registry.getOrCreate(joinCode);
        boolean afterCreate = registry.exists(joinCode);

        // then
        assertThat(beforeCreate).isFalse();
        assertThat(afterCreate).isTrue();
    }

    @Test
    void 모든_방_제거_시_모든_RoundManager가_정리된다() {
        // given
        JoinCode joinCode1 = new JoinCode("TEST1");
        JoinCode joinCode2 = new JoinCode("TEST2");
        
        RoomRoundManager manager1 = mock(RoomRoundManager.class);
        RoomRoundManager manager2 = mock(RoomRoundManager.class);
        
        when(roundManagerFactory.create(joinCode1)).thenReturn(manager1);
        when(roundManagerFactory.create(joinCode2)).thenReturn(manager2);
        
        registry.getOrCreate(joinCode1);
        registry.getOrCreate(joinCode2);

        // when
        registry.removeAll();

        // then
        verify(manager1).cleanup();
        verify(manager2).cleanup();
        assertThat(registry.getActiveRoomCount()).isEqualTo(0);
    }
}
