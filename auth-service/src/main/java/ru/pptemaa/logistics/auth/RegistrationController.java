package ru.pptemaa.logistics.auth;

import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {
    private final RegistrationService registration;

    public RegistrationController(RegistrationService registration) {
        this.registration = registration;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        User user = registration.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(user.id(), user.firstName(), user.lastName(),
                        user.email(), user.registeredAt()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> duplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
    }

    public record RegistrationResponse(long id, String firstName, String lastName,
                                       String email, Instant registeredAt) {
    }
}
