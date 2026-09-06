package coffeeshout.admin.auth.ui.request;

import jakarta.validation.constraints.NotBlank;

public record AdminDevLoginRequest(
        @NotBlank(message = "이메일은 필수입니다.") String email) {}
