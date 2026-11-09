package ru.pptemaa.logistics.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @Positive long userId,
        @NotBlank String deliveryAddress,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @Positive long productId,
            @NotBlank String productName,
            @NotNull @DecimalMin(value = "0.00", inclusive = false)
            @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
            @Positive int quantity
    ) {
    }
}
