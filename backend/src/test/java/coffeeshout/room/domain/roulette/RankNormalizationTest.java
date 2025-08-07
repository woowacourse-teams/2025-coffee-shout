package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RankNormalizationTest {

    @Test
    void 동점자가_없을_때는_기존_등수를_유지한다() {
        // given
        Player 루키 = PlayerFixture.게스트루키();
        Player 꾹이 = PlayerFixture.게스트꾹이();
        Player 한스 = PlayerFixture.호스트한스();
        Player 엠제이 = PlayerFixture.게스트엠제이();

        MiniGameResult miniGameResult = new MiniGameResult(Map.of(
                루키, 1,
                꾹이, 2,
                한스, 3,
                엠제이, 4
        ));

        // when
        RankNormalization rankNormalization = RankNormalization.from(miniGameResult);

        // then
        assertThat(rankNormalization.getNormalizedRank(루키)).isEqualTo(1);
        assertThat(rankNormalization.getNormalizedRank(꾹이)).isEqualTo(2);
        assertThat(rankNormalization.getNormalizedRank(한스)).isEqualTo(3);
        assertThat(rankNormalization.getNormalizedRank(엠제이)).isEqualTo(4);
    }

    @Test
    void 여러_동점자가_있을_때_등수를_정규화한다() {
        // given
        Player 플레이어1 = PlayerFixture.게스트루키();
        Player 플레이어2 = PlayerFixture.게스트꾹이();
        Player 플레이어3 = PlayerFixture.호스트한스();
        Player 플레이어4 = PlayerFixture.게스트엠제이();
        Player 플레이어5 = Player.createGuest(new PlayerName("플레이어5"), null);

        // 1등, 2등, 2등, 4등, 4등 -> 1등, 2등, 2등, 3등, 3등으로 정규화
        MiniGameResult miniGameResult = new MiniGameResult(Map.of(
                플레이어1, 1,
                플레이어2, 2,
                플레이어3, 2,
                플레이어4, 4,
                플레이어5, 4
        ));

        // when
        RankNormalization rankNormalization = RankNormalization.from(miniGameResult);

        // then
        assertThat(rankNormalization.getNormalizedRank(플레이어1)).isEqualTo(1);
        assertThat(rankNormalization.getNormalizedRank(플레이어2)).isEqualTo(2);
        assertThat(rankNormalization.getNormalizedRank(플레이어3)).isEqualTo(2);
        assertThat(rankNormalization.getNormalizedRank(플레이어4)).isEqualTo(3);
        assertThat(rankNormalization.getNormalizedRank(플레이어5)).isEqualTo(3);
    }
}