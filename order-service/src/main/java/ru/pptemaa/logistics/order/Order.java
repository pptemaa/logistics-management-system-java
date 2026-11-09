package ru.pptemaa.logistics.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Order(long id, long userId, OrderStatus status, String deliveryAddress,
                    BigDecimal totalAmount, Long driverId, Instant createdAt,
                    List<Item> items) {
    public record Item(long productId, String productName, BigDecimal unitPrice,
                       int quantity, BigDecimal lineTotal) {
    }
}
