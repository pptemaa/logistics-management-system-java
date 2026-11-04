package ru.pptemaa.logistics.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }

    @Override
    public User save(String firstName, String lastName, String email, String passwordHash) {
        User user = new User(nextId.getAndIncrement(), firstName, lastName, email,
                passwordHash, Instant.now());
        if (users.putIfAbsent(email, user) != null) {
            throw new DuplicateEmailException();
        }
        return user;
    }
}
