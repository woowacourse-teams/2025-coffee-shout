package coffeeshout.minigame.ui;

public class CardGameSelectMessage {
    private String playerName;
    private Integer cardIndex;

    public CardGameSelectMessage() {
        // 기본 생성자 필수
    }

    public CardGameSelectMessage(String playerName, Integer cardIndex) {
        this.playerName = playerName;
        this.cardIndex = cardIndex;
    }

    public String playerName() {
        return playerName;
    }

    public Integer cardIndex() {
        return cardIndex;
    }
}
