package ru.pptemaa.logistics.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreateOrderRequest(
        @Positive long userId,
        @NotBlank String deliveryAddress,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @Positive long productId,
            @Positive int quantity
    ) {
    }
}
