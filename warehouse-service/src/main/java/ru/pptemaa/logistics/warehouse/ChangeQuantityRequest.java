package ru.pptemaa.logistics.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeQuantityRequest(@NotNull @Positive Integer quantity) {
}
