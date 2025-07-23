package coffeeshout.minigame.ui;

import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.ui.MiniGameRanksMessage.MiniGameRankMessage;
import coffeeshout.minigame.ui.MiniGameStateMessage.CardInfoMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Test
    void 카드_게임을_시작한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();
        Long roomId = 1L;

        String subscribeUrlFormat = "/topic/room/%d/gameState";
        String requestUrlFormat = "/app/room/%d/cardGame/start";

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, roomId),
                MiniGameStateMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, roomId), "");

        // then
        MiniGameStateMessage result = responses.get();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.currentRound()).isEqualTo(CardGameRound.FIRST.name());
            softly.assertThat(result.cardInfoMessages()).hasSize(9);
            softly.assertThat(result.allSelected()).isFalse();
        });
    }

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();
        Long roomId = 1L;

        startCardGame(session, roomId);

        String subscribeUrlFormat = "/topic/room/%d/gameState";
        String requestUrlFormat = "/app/room/%d/cardGame/select";

        String playerName = "꾹이";
        int cardIndex = 0;

        CardGameSelectMessage request = new CardGameSelectMessage(playerName, cardIndex);

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, roomId),
                MiniGameStateMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, roomId), request);

        // then
        MiniGameStateMessage result = responses.get();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.currentRound()).isEqualTo(CardGameRound.FIRST.name());
            softly.assertThat(result.cardInfoMessages()).extracting(CardInfoMessage::playerName)
                    .contains(playerName);
            softly.assertThat(result.allSelected()).isFalse();
        });
    }

    @Test
    void 카드_게임의_결과를_조회한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();
        Long roomId = 1L;

        startCardGame(session, roomId);

        String subscribeUrlFormat = "/topic/room/%d/rank";
        String requestUrlFormat = "/app/room/%d/cardGame/rank";

        MessageCollector<MiniGameRanksMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, roomId),
                MiniGameRanksMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, roomId), "");

        // then
        MiniGameRanksMessage result = responses.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.ranks()).hasSize(4);
            softly.assertThat(result.ranks()).extracting(MiniGameRankMessage::rank)
                    .containsExactly(1, 1, 1, 1);
        });
    }

    private void startCardGame(TestStompSession session, Long roomId){
        String requestUrlFormat = "/app/room/%d/cardGame/start";
        session.send(String.format(requestUrlFormat, roomId), "");
    }
}
