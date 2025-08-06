package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import coffeeshout.minigame.domain.round.RoundPhase;
import coffeeshout.minigame.domain.round.RoundState;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class CardGame implements Playable {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;
    private static final int DEFAULT_MAX_ROUNDS = 2;

    private final Deck deck;
    private PlayerHands playerHands;
    
    // 새로운 라운드 관리 방식만 사용
    private RoundState roundState;

    public CardGame(@NonNull CardGameDeckGenerator deckGenerator) {
        this.deck = deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT);
        this.roundState = new RoundState(1, RoundPhase.READY, DEFAULT_MAX_ROUNDS);
    }
    
    public CardGame(@NonNull CardGameDeckGenerator deckGenerator, int maxRounds) {
        this.deck = deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT);
        this.roundState = new RoundState(1, RoundPhase.READY, maxRounds);
    }

    @Override
    public MiniGameResult getResult() {
        return MiniGameResult.from(getScores());
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }

    @Override
    public void startGame(List<Player> players) {
        playerHands = new PlayerHands(players);
    }

    @Override
    public Map<Player, MiniGameScore> getScores() {
        return playerHands.scoreByPlayer();
    }

    public void startPlay() {
        deck.shuffle();
    }

    public void selectCard(Player player, Integer cardIndex) {
        if (!roundState.isPlayingPhase()) {
            throw new IllegalStateException("현재 카드 선택 가능한 단계가 아닙니다. 현재 단계: " + roundState.phase());
        }
        
        playerHands.put(player, deck.pick(cardIndex));
    }

    public boolean isFinishedThisRound() {
        return allPlayersSelected();
    }

    public Player findPlayerByName(PlayerName name) {
        return playerHands.findPlayerByName(name);
    }

    public void assignRandomCardsToUnselectedPlayers() {
        final List<Player> unselectedPlayers = playerHands.getUnselectedPlayers(roundState.roundNumber());
        for (Player player : unselectedPlayers) {
            Card card = deck.pickRandom();
            playerHands.put(player, card);
        }
    }

    public Optional<Player> findCardOwnerInCurrentRound(Card card) {
        return playerHands.findCardOwner(card, roundState.roundNumber());
    }

    public void changeScoreBoardState() {
        // 점수판 상태 변경 로직 (필요시 추가)
    }

    public void changeDoneState() {
        // 게임 완료 상태 변경 로직 (필요시 추가)
    }
    
    // === 새로운 라운드 관리 메서드들 ===
    
    /**
     * 모든 플레이어가 현재 라운드에서 카드를 선택했는지 확인
     */
    public boolean allPlayersSelected() {
        return playerHands.allSelectedInCurrentRound(roundState.roundNumber());
    }

    /**
     * 라운드 점수 계산
     */
    public void calculateRoundScore() {
        // 현재는 실시간으로 점수가 계산되므로 특별한 로직 없음
        // 필요시 라운드별 점수 계산 로직 추가 가능
    }
    
    /**
     * 게임이 완전히 끝났는지 확인
     */
    public boolean isGameCompletelyFinished() {
        return roundState.isGameFinished();
    }
    
    /**
     * 현재 라운드 번호 반환
     */
    public int getCurrentRoundNumber() {
        return roundState.roundNumber();
    }
    
    /**
     * 최대 라운드 수 반환
     */
    public int getMaxRounds() {
        return roundState.maxRounds();
    }
    
    /**
     * 현재 라운드 단계 반환
     */
    public RoundPhase getCurrentPhase() {
        return roundState.phase();
    }

    /**
     * 다음 단계로 이동합니다. CardGame이 자체적으로 RoundState를 관리합니다.
     */
    public void moveToNextPhase() {
        this.roundState = roundState.nextPhase();
    }
}
