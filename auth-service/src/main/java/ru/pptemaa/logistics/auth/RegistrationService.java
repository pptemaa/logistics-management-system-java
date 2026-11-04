package ru.pptemaa.logistics.auth;

import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegistrationService(UserRepository users) {
        this.users = users;
    }

    public User register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        return users.save(request.firstName().trim(), request.lastName().trim(), email,
                passwordEncoder.encode(request.password()));
    }
}
