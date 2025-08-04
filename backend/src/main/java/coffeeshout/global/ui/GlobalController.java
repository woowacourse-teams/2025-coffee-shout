package coffeeshout.global.ui;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("API 서버입니다.");
    }
}
