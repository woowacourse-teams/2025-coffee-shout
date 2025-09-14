package coffeeshout.global.config.redis;

import coffeeshout.global.config.redis.dto.CardDto;
import coffeeshout.global.config.redis.dto.CardHandDto;
import coffeeshout.global.config.redis.dto.MenuDto;
import coffeeshout.global.config.redis.dto.PlayableDto;
import coffeeshout.global.config.redis.dto.PlayerDto;
import coffeeshout.global.config.redis.dto.PlayerHandsDto;
import coffeeshout.global.config.redis.dto.PlayerProbabilityDto;
import coffeeshout.global.config.redis.dto.PlayerScoreDto;
import coffeeshout.global.config.redis.dto.RoomDto;
import coffeeshout.global.config.redis.dto.SelectedMenuDto;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardHand;
import coffeeshout.minigame.domain.cardgame.PlayerHands;
import coffeeshout.minigame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import coffeeshout.minigame.domain.cardgame.card.MultiplierCard;
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
import java.util.ArrayList;
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
                .finishedGames(room.getFinishedGames().stream()
                        .map(this::toPlayableDto)
                        .toList())
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

        // 호스트의 ready 상태 복원 (Redis에서 복원된 값으로 덮어쓰기)
        Player host = room.getHost();
        host.updateReadyState(hostDto.getIsReady());
        log.debug("호스트 ready 상태 복원: name={}, isReady={}", hostDto.getName(), hostDto.getIsReady());

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
        log.debug("플레이어 복원 시작: 전체 플레이어 수={}, 호스트={}", dto.getPlayers().size(), hostDto.getName());
        dto.getPlayers().stream()
                .filter(playerDto -> !playerDto.getName().equals(hostDto.getName()))
                .forEach(playerDto -> {
                    log.debug("게스트 복원 중: playerName={}, type={}, isReady={}", 
                             playerDto.getName(), playerDto.getPlayerType(), playerDto.getIsReady());
                    room.restoreGuest(
                            new PlayerName(playerDto.getName()),
                            toSelectedMenu(playerDto.getSelectedMenu()),
                            playerDto.getIsReady()
                    );
                });
        
        log.debug("플레이어 복원 완료: 현재 Room 플레이어 수={}", room.getPlayers().size());
        room.getPlayers().forEach(p -> 
            log.debug("복원된 플레이어: name={}, type={}", p.getName().value(), p.getPlayerType()));


        // Room 상태 복원 - finishedGames 복원
        log.debug("FinishedGames 복원 시작: finishedGames 수={}", dto.getFinishedGames().size());
        dto.getFinishedGames().forEach(playableDto -> {
            try {
                MiniGameType miniGameType = MiniGameType.valueOf(playableDto.getMiniGameType());
                log.debug("미니게임 복원 시작: type={}, hasPlayerHands={}", 
                         miniGameType, playableDto.getPlayerHands() != null);
                
                Playable finishedGame = miniGameType.createMiniGame();
                
                // 게임 시작 상태로 초기화 (playerHands 초기화)
                finishedGame.startGame(room.getPlayers());
                log.debug("게임 시작 완료: playerCount={}", room.getPlayers().size());
                
                // CardGame의 경우 저장된 PlayerHands 정보 복원
                if (finishedGame instanceof CardGame cardGame && playableDto.getPlayerHands() != null) {
                    log.debug("PlayerHands 복원 시작: 현재 room 플레이어 수={}", room.getPlayers().size());
                    PlayerHands restoredPlayerHands = toPlayerHands(playableDto.getPlayerHands(), room.getPlayers());
                    cardGame.restorePlayerHands(restoredPlayerHands);
                    log.debug("PlayerHands 복원 완료");
                }
                
                room.restoreFinishedGame(finishedGame);
                log.debug("완료된 미니게임 복원: joinCode={}, miniGameType={}", 
                        dto.getJoinCode(), miniGameType);
            } catch (Exception e) {
                log.warn("완료된 미니게임 복원 실패: joinCode={}, miniGameType={}, error={}", 
                        dto.getJoinCode(), playableDto.getMiniGameType(), e.getMessage());
                e.printStackTrace();
            }
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
        PlayerHandsDto playerHandsDto = null;
        
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
            
            // PlayerHands 정보 저장
            try {
                // CardGame에 PlayerHands getter 있는지 확인 후 저장
                java.lang.reflect.Field playerHandsField = CardGame.class.getDeclaredField("playerHands");
                playerHandsField.setAccessible(true);
                PlayerHands playerHands = (PlayerHands) playerHandsField.get(cardGame);
                if (playerHands != null) {
                    playerHandsDto = toPlayerHandsDto(playerHands);
                }
            } catch (Exception e) {
                log.warn("CardGame playerHands 저장 실패: error={}", e.getMessage());
            }
        }

        return new PlayableDto(
                playable.getMiniGameType().name(),
                playable.getClass().getSimpleName(),
                scores,
                gameState,
                playerHandsDto
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

    // CardGame PlayerHands 관련 변환 메서드들
    private CardDto toCardDto(Card card) {
        return new CardDto(
                card.getType().name(),
                card.getValue(),
                card.getClass().getSimpleName()
        );
    }

    private Card toCard(CardDto dto) {
        CardType cardType = CardType.valueOf(dto.getType());
        int value = dto.getValue();
        
        return switch (cardType) {
            case ADDITION -> new AdditionCard(value);
            case MULTIPLIER -> new MultiplierCard(value);
        };
    }

    private CardHandDto toCardHandDto(CardHand cardHand) {
        List<CardDto> cards = new ArrayList<>();
        cardHand.forEach(card -> cards.add(toCardDto(card)));
        return new CardHandDto(cards);
    }

    private CardHand toCardHand(CardHandDto dto) {
        CardHand cardHand = new CardHand();
        for (CardDto cardDto : dto.getCards()) {
            Card card = toCard(cardDto);
            cardHand.put(card);
        }
        return cardHand;
    }

    private PlayerHandsDto toPlayerHandsDto(PlayerHands playerHands) {
        Map<String, CardHandDto> playerHandsMap = new HashMap<>();
        
        for (Map.Entry<Player, CardHand> entry : playerHands.getPlayerHandsMap().entrySet()) {
            String playerName = entry.getKey().getName().value();
            CardHand cardHand = entry.getValue();
            playerHandsMap.put(playerName, toCardHandDto(cardHand));
        }
        
        return new PlayerHandsDto(playerHandsMap);
    }

    private PlayerHands toPlayerHands(PlayerHandsDto dto, List<Player> players) {
        log.debug("PlayerHands 복원: DTO 플레이어 수={}, 실제 플레이어 수={}", 
                  dto.getPlayerHands().size(), players.size());
        
        // DTO에 저장된 플레이어 이름들 출력
        dto.getPlayerHands().keySet().forEach(name -> 
            log.debug("DTO에 저장된 플레이어: {}", name));
        
        // 실제 플레이어들 출력
        players.forEach(p -> 
            log.debug("실제 플레이어: {}", p.getName().value()));
        
        PlayerHands playerHands = new PlayerHands(players);
        
        for (Player player : players) {
            String playerName = player.getName().value();
            log.debug("플레이어 처리 중: {}", playerName);
            
            if (dto.getPlayerHands().containsKey(playerName)) {
                CardHandDto cardHandDto = dto.getPlayerHands().get(playerName);
                log.debug("카드 복원: player={}, cards={}", playerName, cardHandDto.getCards().size());
                
                for (CardDto cardDto : cardHandDto.getCards()) {
                    Card card = toCard(cardDto);
                    playerHands.put(player, card);
                    log.debug("카드 추가: player={}, cardType={}, cardValue={}", 
                             playerName, cardDto.getType(), cardDto.getValue());
                }
            } else {
                log.warn("PlayerHands DTO에서 플레이어를 찾을 수 없음: {}, DTO keys={}", 
                        playerName, dto.getPlayerHands().keySet());
            }
        }
        
        log.debug("PlayerHands 복원 완료");
        return playerHands;
    }
}
