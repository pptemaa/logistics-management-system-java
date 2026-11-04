package ru.pptemaa.logistics.auth;

import java.time.Instant;

public record User(long id, String firstName, String lastName, String email,
                   String passwordHash, Instant registeredAt) {
}
