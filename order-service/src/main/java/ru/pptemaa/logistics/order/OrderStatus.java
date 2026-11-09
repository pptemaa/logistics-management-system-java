package ru.pptemaa.logistics.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING, CONFIRMED, ROUTE_READY, ASSIGNED, IN_PROGRESS, DELIVERED, CANCELLED, FAILED;

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED, FAILED),
            CONFIRMED, Set.of(ROUTE_READY, CANCELLED, FAILED),
            ROUTE_READY, Set.of(ASSIGNED, CANCELLED, FAILED),
            ASSIGNED, Set.of(IN_PROGRESS, CANCELLED, FAILED),
            IN_PROGRESS, Set.of(DELIVERED, FAILED)
    );

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static OrderStatus fromJson(String value) {
        return OrderStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }

    public boolean canTransitionTo(OrderStatus next) {
        return NEXT.getOrDefault(this, Set.of()).contains(next);
    }
}
