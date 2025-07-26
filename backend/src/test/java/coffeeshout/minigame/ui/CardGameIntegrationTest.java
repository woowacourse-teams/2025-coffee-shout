package coffeeshout.minigame.ui;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.ui.MiniGameRanksMessage.MiniGameRankMessage;
import coffeeshout.minigame.ui.MiniGameStateMessage.CardInfoMessage;
import coffeeshout.minigame.ui.handler.SelectCardCommand;
import coffeeshout.minigame.ui.request.CommandType;
import coffeeshout.minigame.ui.request.MiniGameMessage;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.FixedLastValueGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.Roulette;
import coffeeshout.room.domain.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.Session;
import org.hibernate.mapping.Join;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    CardGameService cardGameService;

    @Autowired
    ObjectMapper objectMapper;

    JoinCode joinCode;

    Player host;

    @BeforeEach
    void setUp() {
        joinCode = new JoinCode("ABCDE");

        host = PlayerFixture.꾹이();

        List<Player> guests = List.of(
                PlayerFixture.루키(),
                PlayerFixture.한스(),
                PlayerFixture.엠제이()
        );

        Room room = new Room(joinCode, new Roulette(new FixedLastValueGenerator()), host);
        guests.forEach(room::joinPlayer);
        roomRepository.save(room);
    }

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());
        cardGameService.startGame(joinCode);

        TestStompSession session = createSession();

        Thread.sleep(3200);

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
        );

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode),
                MiniGameStateMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        MiniGameStateMessage result = responses.get();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.currentRound()).isEqualTo(CardGameRound.FIRST.name());
            softly.assertThat(result.cardInfoMessages()).extracting(CardInfoMessage::playerName)
                    .contains(playerName);
            softly.assertThat(result.allSelected()).isFalse();
        });
    }
}
