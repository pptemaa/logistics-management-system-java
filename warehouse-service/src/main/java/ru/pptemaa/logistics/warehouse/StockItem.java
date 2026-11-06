package ru.pptemaa.logistics.warehouse;

import java.math.BigDecimal;
import java.time.Instant;

public record StockItem(long productId, String productName, int quantity,
                        BigDecimal price, Instant lastUpdated) {
}
