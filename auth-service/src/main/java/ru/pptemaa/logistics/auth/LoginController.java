package ru.pptemaa.logistics.auth;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final UserRepository users;
    private final JwtTokenService tokens;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public LoginController(UserRepository users, JwtTokenService tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!passwords.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        JwtTokenService.IssuedToken token = tokens.issue(user);
        return new LoginResponse(token.value(), "Bearer", token.expiresAt());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    public record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
    }
}
