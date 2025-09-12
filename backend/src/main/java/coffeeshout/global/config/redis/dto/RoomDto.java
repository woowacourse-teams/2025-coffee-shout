package coffeeshout.global.config.redis.dto;

import coffeeshout.room.domain.RoomState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoomDto {
    
    private final String joinCode;
    private final String qrCodeUrl;
    private final List<PlayerDto> players;
    private final PlayerDto host;
    private final String roomState;
    private final List<PlayerProbabilityDto> probabilities;
    private final List<PlayableDto> miniGames;
    private final List<PlayableDto> finishedGames;

    @JsonCreator
    public RoomDto(
            @JsonProperty("joinCode") String joinCode,
            @JsonProperty("qrCodeUrl") String qrCodeUrl,
            @JsonProperty("players") List<PlayerDto> players,
            @JsonProperty("host") PlayerDto host,
            @JsonProperty("roomState") String roomState,
            @JsonProperty("probabilities") List<PlayerProbabilityDto> probabilities,
            @JsonProperty("miniGames") List<PlayableDto> miniGames,
            @JsonProperty("finishedGames") List<PlayableDto> finishedGames
    ) {
        this.joinCode = joinCode;
        this.qrCodeUrl = qrCodeUrl;
        this.players = players;
        this.host = host;
        this.roomState = roomState;
        this.probabilities = probabilities;
        this.miniGames = miniGames;
        this.finishedGames = finishedGames;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String joinCode;
        private String qrCodeUrl;
        private List<PlayerDto> players;
        private PlayerDto host;
        private String roomState;
        private List<PlayerProbabilityDto> probabilities;
        private List<PlayableDto> miniGames;
        private List<PlayableDto> finishedGames;

        public Builder joinCode(String joinCode) {
            this.joinCode = joinCode;
            return this;
        }

        public Builder qrCodeUrl(String qrCodeUrl) {
            this.qrCodeUrl = qrCodeUrl;
            return this;
        }

        public Builder players(List<PlayerDto> players) {
            this.players = players;
            return this;
        }

        public Builder host(PlayerDto host) {
            this.host = host;
            return this;
        }

        public Builder roomState(String roomState) {
            this.roomState = roomState;
            return this;
        }

        public Builder probabilities(List<PlayerProbabilityDto> probabilities) {
            this.probabilities = probabilities;
            return this;
        }

        public Builder miniGames(List<PlayableDto> miniGames) {
            this.miniGames = miniGames;
            return this;
        }

        public Builder finishedGames(List<PlayableDto> finishedGames) {
            this.finishedGames = finishedGames;
            return this;
        }

        public RoomDto build() {
            return new RoomDto(joinCode, qrCodeUrl, players, host, roomState, 
                             probabilities, miniGames, finishedGames);
        }
    }
}
