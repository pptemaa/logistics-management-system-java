package ru.pptemaa.logistics.auth;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(String firstName, String lastName, String email, String passwordHash);
}
