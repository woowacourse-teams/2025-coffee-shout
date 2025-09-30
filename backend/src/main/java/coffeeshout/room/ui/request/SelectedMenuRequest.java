package coffeeshout.room.ui.request;

import coffeeshout.room.domain.menu.MenuTemperature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SelectedMenuRequest(@NotNull Long id, @NotBlank String customName,
                                  @NotNull MenuTemperature temperature) {

}
