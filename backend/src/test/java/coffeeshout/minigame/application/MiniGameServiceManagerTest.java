package coffeeshout.minigame.application;


import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.PlayerHands;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class MiniGameServiceManagerTest extends ServiceTest {

    @Autowired
    MiniGameServiceManager miniGameServiceManager;

    @Autowired
    CardGameCommandService cardGameCommandService;

    @Test
    void 미니게임의_점수를_반환한다() {
        // given
        final JoinCode joinCode = new JoinCode("A4B5N");
        final List<Player> players = PlayersFixture.호스트꾹이_루키_엠제이_한스().getPlayers();
        final CardGame cardGame = new CardGame(players, joinCode, new CardGameRandomDeckGenerator());
        final PlayerHands playerHands = new PlayerHands(players);

        playerHands.put(new PlayerName("꾹이"), new Card(CardType.ADDITION, 40));
        playerHands.put(new PlayerName("루키"), new Card(CardType.ADDITION, 30));
        playerHands.put(new PlayerName("엠제이"), new Card(CardType.ADDITION, 20));
        playerHands.put(new PlayerName("한스"), new Card(CardType.ADDITION, 10));

        ReflectionTestUtils.setField(cardGame, "playerHands", playerHands);
        cardGameCommandService.save(cardGame);

        // when
        Map<Player, MiniGameScore> miniGameScores = miniGameServiceManager.getMiniGameScores(
                joinCode.getValue(),
                MiniGameType.CARD_GAME
        );

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(miniGameScores.get(PlayerFixture.호스트꾹이()).getValue()).isEqualTo(40);
            softly.assertThat(miniGameScores.get(PlayerFixture.게스트루키()).getValue()).isEqualTo(30);
            softly.assertThat(miniGameScores.get(PlayerFixture.게스트엠제이()).getValue()).isEqualTo(20);
            softly.assertThat(miniGameScores.get(PlayerFixture.게스트한스()).getValue()).isEqualTo(10);
        });
    }

    @Test
    void 미니게임의_순위를_반환한다() {
        // given
        final JoinCode joinCode = new JoinCode("A4B5N");
        final List<Player> players = PlayersFixture.호스트꾹이_루키_엠제이_한스().getPlayers();
        final CardGame cardGame = new CardGame(players, joinCode, new CardGameRandomDeckGenerator());
        final PlayerHands playerHands = new PlayerHands(players);

        playerHands.put(new PlayerName("꾹이"), new Card(CardType.ADDITION, 40));
        playerHands.put(new PlayerName("루키"), new Card(CardType.ADDITION, 30));
        playerHands.put(new PlayerName("엠제이"), new Card(CardType.ADDITION, 20));
        playerHands.put(new PlayerName("한스"), new Card(CardType.ADDITION, 10));

        ReflectionTestUtils.setField(cardGame, "playerHands", playerHands);
        cardGameCommandService.save(cardGame);

        // when
        Map<Player, Integer> rank = miniGameServiceManager.getMiniGameRanks(
                joinCode.getValue(),
                MiniGameType.CARD_GAME
        ).getRank();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rank.get(PlayerFixture.호스트꾹이())).isEqualTo(1);
            softly.assertThat(rank.get(PlayerFixture.호스트루키())).isEqualTo(2);
            softly.assertThat(rank.get(PlayerFixture.호스트엠제이())).isEqualTo(3);
            softly.assertThat(rank.get(PlayerFixture.호스트한스())).isEqualTo(4);
        });
    }
}
