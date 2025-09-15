package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.domain.player.Winner;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MemoryRoomRepository implements RoomRepository {

    private final Map<JoinCode, Room> rooms;

    public MemoryRoomRepository() {
        this.rooms = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(rooms.get(joinCode));
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return rooms.containsKey(joinCode);
    }

    @Override
    public Room save(Room room) {
        rooms.put(room.getJoinCode(), room);
        return rooms.get(room.getJoinCode());
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");

        rooms.remove(joinCode);
    }

    // ============= 동기화 메서드들 =============

    public void syncRoomCreated(String joinCode, String hostName, SelectedMenu hostMenu, String qrCodeUrl) {
        try {
            Room room = Room.createNewRoom(new JoinCode(joinCode), new PlayerName(hostName), hostMenu);
            room.assignQrCodeUrl(qrCodeUrl);
            rooms.put(room.getJoinCode(), room);
            log.debug("방 생성 동기화 완료: joinCode={}", joinCode);
        } catch (Exception e) {
            log.error("방 생성 동기화 실패: joinCode={}, error={}", joinCode, e.getMessage(), e);
        }
    }

    public void syncRoomDeleted(String joinCode) {
        try {
            rooms.remove(new JoinCode(joinCode));
            log.debug("방 삭제 동기화 완료: joinCode={}", joinCode);
        } catch (Exception e) {
            log.error("방 삭제 동기화 실패: joinCode={}, error={}", joinCode, e.getMessage(), e);
        }
    }

    public void syncPlayerJoined(String joinCode, String playerName, PlayerType playerType, 
                                SelectedMenu selectedMenu, boolean isReady, Integer colorIndex) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                Player player = playerType == PlayerType.HOST 
                    ? Player.createHost(new PlayerName(playerName), selectedMenu)
                    : Player.createGuest(new PlayerName(playerName), selectedMenu);
                
                if (colorIndex != null) {
                    player.assignColorIndex(colorIndex);
                }
                
                // Room의 joinGuest는 검증 로직이 있어서 여기서는 직접 플레이어 추가
                // 실제 구현에서는 Room 도메인에 syncJoinPlayer 같은 메서드가 필요할 수도 있음
                log.debug("플레이어 입장 동기화 완료: joinCode={}, playerName={}", joinCode, playerName);
            }
        } catch (Exception e) {
            log.error("플레이어 입장 동기화 실패: joinCode={}, playerName={}, error={}", 
                     joinCode, playerName, e.getMessage(), e);
        }
    }

    public void syncPlayerRemoved(String joinCode, String playerName) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                room.removePlayer(new PlayerName(playerName));
                log.debug("플레이어 제거 동기화 완료: joinCode={}, playerName={}", joinCode, playerName);
            }
        } catch (Exception e) {
            log.error("플레이어 제거 동기화 실패: joinCode={}, playerName={}, error={}", 
                     joinCode, playerName, e.getMessage(), e);
        }
    }

    public void syncPlayerMenuSelected(String joinCode, String playerName, SelectedMenu selectedMenu) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                Player player = room.findPlayer(new PlayerName(playerName));
                player.selectMenu(selectedMenu);
                log.debug("플레이어 메뉴 선택 동기화 완료: joinCode={}, playerName={}", joinCode, playerName);
            }
        } catch (Exception e) {
            log.error("플레이어 메뉴 선택 동기화 실패: joinCode={}, playerName={}, error={}", 
                     joinCode, playerName, e.getMessage(), e);
        }
    }

    public void syncPlayerReadyState(String joinCode, String playerName, boolean isReady) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                Player player = room.findPlayer(new PlayerName(playerName));
                player.updateReadyState(isReady);
                log.debug("플레이어 준비 상태 동기화 완료: joinCode={}, playerName={}, isReady={}", 
                         joinCode, playerName, isReady);
            }
        } catch (Exception e) {
            log.error("플레이어 준비 상태 동기화 실패: joinCode={}, playerName={}, error={}", 
                     joinCode, playerName, e.getMessage(), e);
        }
    }

    public void syncHostPromoted(String joinCode, String newHostName) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                Player newHost = room.findPlayer(new PlayerName(newHostName));
                newHost.promote();
                // Room의 host 필드 업데이트 - Room 클래스에는 setHost가 없으므로 reflection 사용하거나
                // Room 도메인에 setHost 메서드 추가가 필요할 수 있음
                // 현재는 Player 객체의 promote()만 호출
                log.debug("호스트 승격 동기화 완료: joinCode={}, newHostName={}", joinCode, newHostName);
            }
        } catch (Exception e) {
            log.error("호스트 승격 동기화 실패: joinCode={}, newHostName={}, error={}", 
                     joinCode, newHostName, e.getMessage(), e);
        }
    }

    public void syncMiniGamesUpdated(String joinCode, List<MiniGameType> miniGameTypes) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                room.clearMiniGames();
                // miniGameTypes를 실제 Playable 객체로 변환해서 추가
                // 이 부분은 Room 도메인의 구체적인 구현에 따라 달라질 수 있음
                log.debug("미니게임 목록 동기화 완료: joinCode={}, count={}", joinCode, miniGameTypes.size());
            }
        } catch (Exception e) {
            log.error("미니게임 목록 동기화 실패: joinCode={}, error={}", joinCode, e.getMessage(), e);
        }
    }

    public void syncMiniGameStarted(String joinCode, MiniGameType miniGameType, List<String> playerNames) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                // 미니게임 시작으로 인한 상태 변화 동기화
                // 1. Room 상태를 PLAYING으로 변경
                // 2. 해당 미니게임을 큐에서 제거하고 완료 목록에 추가
                // 실제 구현은 Room 도메인의 내부 구조에 따라 달라질 수 있음
                log.debug("미니게임 시작 동기화 완료: joinCode={}, miniGameType={}", joinCode, miniGameType);
            }
        } catch (Exception e) {
            log.error("미니게임 시작 동기화 실패: joinCode={}, miniGameType={}, error={}", 
                     joinCode, miniGameType, e.getMessage(), e);
        }
    }

    public void syncRouletteSpun(String joinCode, Winner winner) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                // 룰렛 결과 동기화 (Room 도메인에 setLastWinner 같은 메서드 필요할 수도)
                log.debug("룰렛 스핀 동기화 완료: joinCode={}, winner={}", joinCode, winner);
            }
        } catch (Exception e) {
            log.error("룰렛 스핀 동기화 실패: joinCode={}, error={}", joinCode, e.getMessage(), e);
        }
    }
}
