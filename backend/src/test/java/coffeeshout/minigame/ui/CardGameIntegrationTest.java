package coffeeshout.minigame.ui;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage.CardInfoMessage;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import coffeeshout.minigame.ui.request.CommandType;
import coffeeshout.minigame.ui.request.MiniGameMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
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
        joinCode = new JoinCode("A4B2C");

        Room room = RoomFixture.호스트_꾹이();
        host = room.getHost();
        roomRepository.save(room);
    }

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        cardGameService.startGame(joinCode);

        MiniGameStateMessage loadingState = responses.get(); // 게임 로딩 state 응답 (LOADING)
        assertThat(loadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        MiniGameStateMessage playingState = responses.get(); // 게임 시작 state 응답 (PLAYING)
        assertThat(playingState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
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
