package coffeeshout.global.config.redis;

import coffeeshout.global.config.redis.dto.MenuDto;
import coffeeshout.global.config.redis.dto.PlayableDto;
import coffeeshout.global.config.redis.dto.PlayerDto;
import coffeeshout.global.config.redis.dto.PlayerProbabilityDto;
import coffeeshout.global.config.redis.dto.PlayerScoreDto;
import coffeeshout.global.config.redis.dto.RoomDto;
import coffeeshout.global.config.redis.dto.SelectedMenuDto;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.menu.CustomMenu;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuTemperature;
import coffeeshout.room.domain.menu.ProvidedMenu;
import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.MenuQueryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomDtoConverter {

    private final MenuQueryService menuQueryService;

    /**
     * Room -> RoomDto 변환
     */
    public RoomDto toDto(Room room) {
        return RoomDto.builder()
                .joinCode(room.getJoinCode().getValue())
                .qrCodeUrl(room.getJoinCode().getQrCodeUrl())
                .players(room.getPlayers().stream()
                        .map(this::toPlayerDto)
                        .toList())
                .host(toPlayerDto(room.getHost()))
                .roomState(room.getRoomState().name())
                .probabilities(convertProbabilities(room.getProbabilities()))
                .miniGames(room.getAllMiniGame().stream()
                        .map(this::toPlayableDto)
                        .toList())
                .finishedGames(Collections.emptyList()) // finishedGames는 일단 빈 리스트로
                .build();
    }

    /**
     * RoomDto -> Room 변환
     */
    public Room toRoom(RoomDto dto) {
        // JoinCode 생성
        JoinCode joinCode = new JoinCode(dto.getJoinCode());
        if (dto.getQrCodeUrl() != null) {
            joinCode.assignQrCodeUrl(dto.getQrCodeUrl());
        }

        // 호스트 정보로 Room 생성
        PlayerDto hostDto = dto.getHost();
        PlayerName hostName = new PlayerName(hostDto.getName());
        SelectedMenu hostSelectedMenu = toSelectedMenu(hostDto.getSelectedMenu());

        Room room = Room.createNewRoom(joinCode, hostName, hostSelectedMenu);

        // roomState 복원
        if (dto.getRoomState() != null) {
            try {
                RoomState roomState = RoomState.valueOf(dto.getRoomState());
                room.restoreRoomState(roomState);
                log.debug("roomState 복원: joinCode={}, roomState={}", dto.getJoinCode(), roomState);
            } catch (Exception e) {
                log.warn("roomState 복원 실패: joinCode={}, roomState={}, error={}",
                        dto.getJoinCode(), dto.getRoomState(), e.getMessage());
            }
        }

        // 나머지 플레이어들 추가 (호스트 제외)
        dto.getPlayers().stream()
                .filter(playerDto -> !playerDto.getName().equals(hostDto.getName()))
                .forEach(playerDto -> {
                    room.restoreGuest(
                            new PlayerName(playerDto.getName()),
                            toSelectedMenu(playerDto.getSelectedMenu())
                    );
                });

        // Room 상태 복원 - miniGames 복원
        dto.getMiniGames().forEach(playableDto -> {
            try {
                MiniGameType miniGameType = MiniGameType.valueOf(playableDto.getMiniGameType());
                Playable miniGame = miniGameType.createMiniGame();
                room.addMiniGame(hostName, miniGame);
                log.debug("미니게임 복원: joinCode={}, miniGameType={}",
                        dto.getJoinCode(), miniGameType);
            } catch (Exception e) {
                log.warn("미니게임 복원 실패: joinCode={}, miniGameType={}, error={}",
                        dto.getJoinCode(), playableDto.getMiniGameType(), e.getMessage());
            }
        });

        log.debug("Room 복원 완료: joinCode={}, players={}, miniGames={}",
                dto.getJoinCode(), dto.getPlayers().size(), dto.getMiniGames().size());

        return room;
    }

    private PlayerDto toPlayerDto(Player player) {
        return new PlayerDto(
                player.getName().value(),
                player.getPlayerType().name(),
                toSelectedMenuDto(player.getSelectedMenu()),
                player.getIsReady(),
                player.getColorIndex()
        );
    }

    private SelectedMenuDto toSelectedMenuDto(SelectedMenu selectedMenu) {
        return new SelectedMenuDto(
                toMenuDto(selectedMenu.menu()),
                selectedMenu.menuTemperature().name()
        );
    }

    private MenuDto toMenuDto(Menu menu) {
        if (menu instanceof ProvidedMenu providedMenu) {
            return new MenuDto(
                    "PROVIDED",
                    providedMenu.getId(),
                    providedMenu.getName(),
                    providedMenu.getCategoryImageUrl(),
                    providedMenu.getTemperatureAvailability().name()
            );
        } else if (menu instanceof CustomMenu customMenu) {
            return new MenuDto(
                    "CUSTOM",
                    null,
                    customMenu.getName(),
                    customMenu.getCategoryImageUrl(),
                    customMenu.getTemperatureAvailability().name()
            );
        } else {
            throw new IllegalArgumentException("지원하지 않는 Menu 타입: " + menu.getClass());
        }
    }

    private List<PlayerProbabilityDto> convertProbabilities(Map<Player, Probability> probabilities) {
        return probabilities.entrySet().stream()
                .map(entry -> new PlayerProbabilityDto(
                        entry.getKey().getName().value(),
                        entry.getValue().value()
                ))
                .toList();
    }

    private PlayableDto toPlayableDto(Playable playable) {
        List<PlayerScoreDto> scores;
        try {
            scores = playable.getScores().entrySet().stream()
                    .map(entry -> new PlayerScoreDto(
                            entry.getKey().getName().value(),
                            entry.getValue().getValue()
                    ))
                    .toList();
        } catch (Exception e) {
            // 게임이 아직 시작되지 않은 경우 빈 점수 반환
            log.debug("게임 점수 조회 실패 (아직 시작되지 않음): miniGameType={}, error={}",
                    playable.getMiniGameType(), e.getMessage());
            scores = Collections.emptyList();
        }

        Map<String, Object> gameState = new HashMap<>();

        // CardGame의 경우 추가 상태 정보 저장
        if (playable instanceof CardGame cardGame) {
            gameState.put("round", cardGame.getRound().name());
            gameState.put("state", cardGame.getState().name());
            // 필요한 다른 상태들도 추가 가능
        }

        return new PlayableDto(
                playable.getMiniGameType().name(),
                playable.getClass().getSimpleName(),
                scores,
                gameState
        );
    }

    private SelectedMenu toSelectedMenu(SelectedMenuDto dto) {
        Menu menu = toMenu(dto.getMenu());
        MenuTemperature temperature = MenuTemperature.valueOf(dto.getMenuTemperature());
        return new SelectedMenu(menu, temperature);
    }

    private Menu toMenu(MenuDto dto) {
        if ("PROVIDED".equals(dto.getType())) {
            // ProvidedMenu는 DB에서 조회
            if (dto.getId() == null) {
                throw new IllegalArgumentException("ProvidedMenu의 ID가 null입니다");
            }
            return menuQueryService.getById(dto.getId());
        } else if ("CUSTOM".equals(dto.getType())) {
            return new CustomMenu(dto.getName(), dto.getCategoryImageUrl());
        } else {
            throw new IllegalArgumentException("지원하지 않는 Menu 타입: " + dto.getType());
        }
    }
}
