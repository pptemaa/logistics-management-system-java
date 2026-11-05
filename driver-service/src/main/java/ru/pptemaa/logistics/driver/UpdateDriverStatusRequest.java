package ru.pptemaa.logistics.driver;

import jakarta.validation.constraints.NotNull;

public record UpdateDriverStatusRequest(@NotNull DriverStatus status) {
}
