package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameSnapshot;
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

    public void syncRoomStateChanged(String joinCode, RoomState newState) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                room.syncSetRoomState(newState);
                log.debug("방 상태 동기화 완료: joinCode={}, newState={}", joinCode, newState);
            }
        } catch (Exception e) {
            log.error("방 상태 동기화 실패: joinCode={}, newState={}, error={}", 
                     joinCode, newState, e.getMessage(), e);
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
                
                if (playerType == PlayerType.GUEST) {
                    player.updateReadyState(isReady);
                }
                
                room.syncJoinPlayer(player);
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
                room.syncSetHost(new PlayerName(newHostName));
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
                miniGameTypes.forEach(type -> {
                    Playable miniGame = type.createMiniGame();
                    room.syncAddMiniGame(miniGame);
                });
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
                room.syncStartMiniGame(miniGameType);
                log.debug("미니게임 시작 동기화 완료: joinCode={}, miniGameType={}", joinCode, miniGameType);
            }
        } catch (Exception e) {
            log.error("미니게임 시작 동기화 실패: joinCode={}, miniGameType={}, error={}", 
                     joinCode, miniGameType, e.getMessage(), e);
        }
    }

    public void syncRouletteSpin(String joinCode, Winner winner) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                // 룰렛 결과로 인해 방 상태가 DONE으로 변경됨
                room.syncSetRoomState(RoomState.DONE);
                log.debug("룰렛 스핀 동기화 완료: joinCode={}, winner={}", joinCode, winner);
            }
        } catch (Exception e) {
            log.error("룰렛 스핀 동기화 실패: joinCode={}, error={}", joinCode, e.getMessage(), e);
        }
    }

    public void syncCardSelected(String joinCode, String playerName, Integer cardIndex) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                // 카드 게임에서 카드 선택 동기화
                Playable cardGame = room.findMiniGame(MiniGameType.CARD_GAME);
                // 실제 카드 선택 로직은 CardGame 도메인 객체에서 처리
                // 여기서는 로그만 남김
                log.debug("카드 선택 동기화 완료: joinCode={}, playerName={}, cardIndex={}", 
                         joinCode, playerName, cardIndex);
            }
        } catch (Exception e) {
            log.error("카드 선택 동기화 실패: joinCode={}, playerName={}, cardIndex={}, error={}", 
                     joinCode, playerName, cardIndex, e.getMessage(), e);
        }
    }

    public void syncCardGameState(String joinCode, MiniGameType gameType, Object gameStateSnapshot) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null && gameType == MiniGameType.CARD_GAME) {
                Playable playable = room.findMiniGame(gameType);
                if (playable instanceof coffeeshout.minigame.domain.cardgame.CardGame cardGame) {
                    // 게임 상태 스냅샷으로부터 복원
                    if (gameStateSnapshot instanceof CardGameSnapshot snapshot) {
                        cardGame.restoreFromSnapshot(snapshot);
                    }
                    log.debug("카드게임 상태 동기화 완료: joinCode={}, gameType={}", joinCode, gameType);
                }
            }
        } catch (Exception e) {
            log.error("카드게임 상태 동기화 실패: joinCode={}, gameType={}, error={}", 
                     joinCode, gameType, e.getMessage(), e);
        }
    }

    public void syncMiniGameCompleted(String joinCode, MiniGameType gameType, Object result) {
        try {
            Room room = rooms.get(new JoinCode(joinCode));
            if (room != null) {
                // 미니게임 완료 동기화
                log.debug("미니게임 완료 동기화: joinCode={}, gameType={}", joinCode, gameType);
            }
        } catch (Exception e) {
            log.error("미니게임 완료 동기화 실패: joinCode={}, gameType={}, error={}", 
                     joinCode, gameType, e.getMessage(), e);
        }
    }
}
