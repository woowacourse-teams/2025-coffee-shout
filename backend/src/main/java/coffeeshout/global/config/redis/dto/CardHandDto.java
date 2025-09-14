package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CardHandDto {
    
    private final List<CardDto> cards;

    @JsonCreator
    public CardHandDto(@JsonProperty("cards") List<CardDto> cards) {
        this.cards = cards;
    }
}
