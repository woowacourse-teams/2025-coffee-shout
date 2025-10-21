package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_META_KEY = "room:%s:meta";
    private static final String ROOM_PLAYERS_KEY = "room:%s:players";
    private static final String ROOM_MINI_GAMES_KEY = "room:%s:miniGames";
    private static final String ROOM_FINISHED_GAMES_KEY = "room:%s:finishedGames";
    private static final Duration ROOM_DEFAULT_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRoomRepository(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        try {
            String joinCodeValue = joinCode.getValue();

            // 1. meta 정보 읽기
            Map<Object, Object> metaMap = redisTemplate.opsForHash()
                    .entries(String.format(ROOM_META_KEY, joinCodeValue));

            if (metaMap.isEmpty()) {
                return Optional.empty();
            }

            // 2. players 읽기
            Map<Object, Object> playersMap = redisTemplate.opsForHash()
                    .entries(String.format(ROOM_PLAYERS_KEY, joinCodeValue));

            // 3. miniGames 읽기
            List<Object> miniGamesList = redisTemplate.opsForList()
                    .range(String.format(ROOM_MINI_GAMES_KEY, joinCodeValue), 0, -1);

            // 4. finishedGames 읽기
            List<Object> finishedGamesList = redisTemplate.opsForList()
                    .range(String.format(ROOM_FINISHED_GAMES_KEY, joinCodeValue), 0, -1);

            // 5. Room 객체 재구성
            return Optional.of(reconstructRoom(joinCode, metaMap, playersMap,
                    miniGamesList != null ? miniGamesList : new ArrayList<>(),
                    finishedGamesList != null ? finishedGamesList : new ArrayList<>()));

        } catch (Exception e) {
            throw new IllegalStateException("Room 조회 중 오류 발생", e);
        }
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(
                String.format(ROOM_META_KEY, joinCode.getValue())
        ));
    }

    @Override
    public Room save(Room room) {
        String joinCodeValue = room.getJoinCode().getValue();

        try {
            // 1. meta 저장
            saveMetaData(joinCodeValue, room);

            // 2. players 저장
            savePlayers(joinCodeValue, room);

            // 3. miniGames 저장
            saveMiniGames(joinCodeValue, room);

            // 4. finishedGames 저장
            saveFinishedGames(joinCodeValue, room);

            // 5. TTL 설정
            setExpiration(joinCodeValue);

            return room;

        } catch (Exception e) {
            throw new IllegalStateException("Room 저장 중 오류 발생", e);
        }
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        String joinCodeValue = joinCode.getValue();

        redisTemplate.delete(String.format(ROOM_META_KEY, joinCodeValue));
        redisTemplate.delete(String.format(ROOM_PLAYERS_KEY, joinCodeValue));
        redisTemplate.delete(String.format(ROOM_MINI_GAMES_KEY, joinCodeValue));
        redisTemplate.delete(String.format(ROOM_FINISHED_GAMES_KEY, joinCodeValue));
    }

    @Override
    public void updatePlayerReadyState(JoinCode joinCode, PlayerName playerName, Boolean isReady) {
        try {
            String playersKey = String.format(ROOM_PLAYERS_KEY, joinCode.getValue());
            String playerJson = (String) redisTemplate.opsForHash().get(playersKey, playerName.value());

            if (playerJson != null) {
                Player player = objectMapper.readValue(playerJson, Player.class);
                player.updateReadyState(isReady);

                redisTemplate.opsForHash().put(
                        playersKey,
                        playerName.value(),
                        objectMapper.writeValueAsString(player)
                );

                redisTemplate.expire(playersKey, ROOM_DEFAULT_TTL);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Player ready 상태 업데이트 중 오류", e);
        }
    }

    @Override
    public void updatePlayers(JoinCode joinCode, Room room) {
        savePlayers(joinCode.getValue(), room);
        redisTemplate.expire(
                String.format(ROOM_PLAYERS_KEY, joinCode.getValue()),
                ROOM_DEFAULT_TTL
        );
    }

    @Override
    public void updateRoomState(JoinCode joinCode, String state) {
        String metaKey = String.format(ROOM_META_KEY, joinCode.getValue());
        redisTemplate.opsForHash().put(metaKey, "state", state);
        redisTemplate.expire(metaKey, ROOM_DEFAULT_TTL);
    }

    @Override
    public void updateMiniGames(JoinCode joinCode, Room room) {
        saveMiniGames(joinCode.getValue(), room);
        saveFinishedGames(joinCode.getValue(), room);

        redisTemplate.expire(
                String.format(ROOM_MINI_GAMES_KEY, joinCode.getValue()),
                ROOM_DEFAULT_TTL
        );
        redisTemplate.expire(
                String.format(ROOM_FINISHED_GAMES_KEY, joinCode.getValue()),
                ROOM_DEFAULT_TTL
        );
    }

    private void saveMetaData(String joinCodeValue, Room room) {
        String metaKey = String.format(ROOM_META_KEY, joinCodeValue);

        redisTemplate.opsForHash().put(metaKey, "hostName", room.getHost().getName().value());
        redisTemplate.opsForHash().put(metaKey, "state", room.getRoomState().name());

        if (room.getJoinCode().getQrCodeUrl() != null) {
            redisTemplate.opsForHash().put(metaKey, "qrCodeUrl", room.getJoinCode().getQrCodeUrl());
        }
    }

    private void savePlayers(String joinCodeValue, Room room) {
        String playersKey = String.format(ROOM_PLAYERS_KEY, joinCodeValue);

        // 기존 players 삭제
        redisTemplate.delete(playersKey);

        // 새로운 players 저장
        for (Player player : room.getPlayers()) {
            try {
                redisTemplate.opsForHash().put(
                        playersKey,
                        player.getName().value(),
                        objectMapper.writeValueAsString(player)
                );
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Player 직렬화 중 오류", e);
            }
        }
    }

    private void saveMiniGames(String joinCodeValue, Room room) {
        String miniGamesKey = String.format(ROOM_MINI_GAMES_KEY, joinCodeValue);

        // 기존 miniGames 삭제
        redisTemplate.delete(miniGamesKey);

        // 새로운 miniGames 저장
        for (MiniGameType gameType : room.getAllMiniGames()) {
            redisTemplate.opsForList().rightPush(miniGamesKey, gameType.name());
        }
    }

    private void saveFinishedGames(String joinCodeValue, Room room) {
        String finishedGamesKey = String.format(ROOM_FINISHED_GAMES_KEY, joinCodeValue);

        // 기존 finishedGames 삭제
        redisTemplate.delete(finishedGamesKey);

        // 새로운 finishedGames 저장
        for (MiniGameType gameType : room.getFinishedGames()) {
            redisTemplate.opsForList().rightPush(finishedGamesKey, gameType.name());
        }
    }

    private void setExpiration(String joinCodeValue) {
        redisTemplate.expire(String.format(ROOM_META_KEY, joinCodeValue), ROOM_DEFAULT_TTL);
        redisTemplate.expire(String.format(ROOM_PLAYERS_KEY, joinCodeValue), ROOM_DEFAULT_TTL);
        redisTemplate.expire(String.format(ROOM_MINI_GAMES_KEY, joinCodeValue), ROOM_DEFAULT_TTL);
        redisTemplate.expire(String.format(ROOM_FINISHED_GAMES_KEY, joinCodeValue), ROOM_DEFAULT_TTL);
    }

    private Room reconstructRoom(
            JoinCode joinCode,
            Map<Object, Object> metaMap,
            Map<Object, Object> playersMap,
            List<Object> miniGamesList,
            List<Object> finishedGamesList
    ) {
        try {
            // Players 재구성
            Players players = new Players();
            for (Map.Entry<Object, Object> entry : playersMap.entrySet()) {
                Player player = objectMapper.readValue((String) entry.getValue(), Player.class);
                players.join(player);
            }

            // Host 설정
            String hostName = (String) metaMap.get("hostName");
            Player host = players.getPlayer(new PlayerName(hostName));

            // RoomState 설정
            RoomState roomState = RoomState.valueOf((String) metaMap.get("state"));

            // MiniGames 재구성
            Queue<MiniGameType> miniGames = new LinkedList<>();
            for (Object gameObj : miniGamesList) {
                miniGames.add(MiniGameType.valueOf((String) gameObj));
            }

            // FinishedGames 재구성
            List<MiniGameType> finishedGames = new ArrayList<>();
            for (Object gameObj : finishedGamesList) {
                finishedGames.add(MiniGameType.valueOf((String) gameObj));
            }

            // QR Code URL 설정
            if (metaMap.containsKey("qrCodeUrl")) {
                joinCode.assignQrCodeUrl((String) metaMap.get("qrCodeUrl"));
            }

            // Room 재구성
            return Room.reconstruct(joinCode, players, miniGames, finishedGames, host, roomState);

        } catch (Exception e) {
            throw new IllegalStateException("Room 재구성 중 오류 발생", e);
        }
    }
}
