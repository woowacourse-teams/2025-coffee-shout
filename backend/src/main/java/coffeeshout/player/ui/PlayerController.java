package coffeeshout.player.ui;

import coffeeshout.player.application.MenuService;
import coffeeshout.player.ui.response.MenuResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final MenuService menuService;

    @GetMapping("/menus")
    public ResponseEntity<List<MenuResponse>> getAllMenu() {
        final List<MenuResponse> responses = menuService.getAll().stream()
                .map(MenuResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
