package coffeeshout.minigame.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.PlayerProbabilities;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@Import({TestConfig.class})
class CardGameServiceTest {

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    CardGameService cardGameService;

    @Autowired
    RoomQueryService roomQueryService;

    @Autowired
    RoomService roomService;

    @Autowired
    CardGameTaskExecutors cardGameTaskExecutors;

    JoinCode joinCode;

    @BeforeEach
    void setUp() {
        List<Player> players = PlayerProbabilities.PLAYERS;
        Player host = players.get(0);
        Room room = roomService.createRoom(host.getName().value(), 1L);
        joinCode = room.getJoinCode();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        for (int i = 1; i < players.size(); i++) {
            room.joinGuest(players.get(i).getName(), MenuFixture.아메리카노());
        }
        MockitoAnnotations.openMocks(this);
    }

    //    @Disabled
    @Test
    void 카드게임이_종료되면_결과에_따라_룰렛의_가중치가_반영된다() throws InterruptedException {
        Room room = roomQueryService.findByJoinCode(joinCode);
        room.startGame(MiniGameType.CARD_GAME);
        CardGame cardGame = (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
        CardGame cardGameSpy = spy(cardGame);
        List<Player> players = room.getPlayers();
        MiniGameResult result = new MiniGameResult(Map.of(
                players.get(0), 1, // 꾹이 1등 / 확률: 0
                players.get(1), 2, // 루키 2등 / 확률: 1250
                players.get(2), 3, // 엠제이 3등 / 확률: 3750
                players.get(3), 4 // 한스 4등 / 확률: 5000
        ));
        doReturn(result).when(cardGameSpy).getResult();
        ReflectionTestUtils.setField(room, "miniGames", List.of(cardGameSpy));
        cardGameService.startGame(joinCode.value());
        Thread.sleep(500);

        Map<Player, Probability> probabilities = room.getProbabilities();
        assertThat(probabilities).containsExactlyInAnyOrderEntriesOf(Map.of(
                    players.get(0), new Probability(0),
                    players.get(1), new Probability(1250),
                    players.get(2), new Probability(3750),
                    players.get(3), new Probability(5000)));
    }

    @Test
    void 카드게임을_시작하면_태스크가_순차적으로_실행된다() throws InterruptedException {
        Room room = roomQueryService.findByJoinCode(joinCode);
        room.startGame(MiniGameType.CARD_GAME);
        cardGameService.startGame(joinCode.value());
        verify(messagingTemplate, atLeast(6))
                .convertAndSend(
                        eq("/topic/room/" + joinCode.getValue() + "/gameState"),
                        any(MiniGameStateMessage.class)
                );
    }
}
