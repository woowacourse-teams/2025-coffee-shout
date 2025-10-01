package coffeeshout.room.ui.request;

import coffeeshout.room.domain.menu.MenuTemperature;
import jakarta.validation.constraints.NotNull;

public record SelectedMenuRequest(@NotNull Long id, @NotNull String customName,
                                  @NotNull MenuTemperature temperature) {

}
