package coffeeshout.minigame.ui;

import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.ui.MiniGameRanksMessage.MiniGameRankMessage;
import coffeeshout.minigame.ui.MiniGameStateMessage.CardInfoMessage;
import coffeeshout.room.domain.JoinCode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();
        String joinCode = "ABCDE";

        startCardGame(session, joinCode);

        Thread.sleep(3200);

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/cardGame/select";

        String playerName = "꾹이";
        int cardIndex = 0;

        CardGameSelectMessage request = new CardGameSelectMessage(playerName, cardIndex);

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode),
                MiniGameStateMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode), request);

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
        String joinCode = "ABCDE";

        startCardGame(session, joinCode);

        String subscribeUrlFormat = "/topic/room/%s/rank";
        String requestUrlFormat = "/app/room/%s/cardGame/rank";

        MessageCollector<MiniGameRanksMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode),
                MiniGameRanksMessage.class
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode), "");

        // then
        MiniGameRanksMessage result = responses.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.ranks()).hasSize(4);
            softly.assertThat(result.ranks()).extracting(MiniGameRankMessage::rank)
                    .containsExactly(1, 1, 1, 1);
        });
    }

    private void startCardGame(TestStompSession session, String joinCode){
        String requestUrlFormat = "/app/room/%s/cardGame/start";
        session.send(String.format(requestUrlFormat, joinCode), "");
    }
}
