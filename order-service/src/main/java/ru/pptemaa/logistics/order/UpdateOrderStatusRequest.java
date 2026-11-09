package ru.pptemaa.logistics.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status, @Positive Long driverId) {
}
