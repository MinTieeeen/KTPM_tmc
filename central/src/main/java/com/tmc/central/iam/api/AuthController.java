package com.tmc.central.iam.api;

import com.tmc.central.iam.application.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code POST /auth/login} (công khai) và {@code GET /auth/me} (mọi vai trò). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase login;
    private final AuthApi authApi;

    public AuthController(LoginUseCase login, AuthApi authApi) {
        this.login = login;
        this.authApi = authApi;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return login.login(request.username(), request.password());
    }

    @GetMapping("/me")
    public CurrentUser me() {
        return authApi.currentUser();
    }
}
