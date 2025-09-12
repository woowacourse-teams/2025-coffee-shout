package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PlayerDto {
    
    private final String name;
    private final String playerType;
    private final SelectedMenuDto selectedMenu;
    private final Boolean isReady;
    private final Integer colorIndex;

    @JsonCreator
    public PlayerDto(
            @JsonProperty("name") String name,
            @JsonProperty("playerType") String playerType,
            @JsonProperty("selectedMenu") SelectedMenuDto selectedMenu,
            @JsonProperty("isReady") Boolean isReady,
            @JsonProperty("colorIndex") Integer colorIndex
    ) {
        this.name = name;
        this.playerType = playerType;
        this.selectedMenu = selectedMenu;
        this.isReady = isReady;
        this.colorIndex = colorIndex;
    }
}
