package ru.pptemaa.logistics.driver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum DriverStatus {
    OFFLINE, AVAILABLE, BUSY, BREAK, UNAVAILABLE;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static DriverStatus fromJson(String value) {
        return DriverStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
