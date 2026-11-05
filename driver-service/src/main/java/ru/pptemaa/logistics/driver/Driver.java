package ru.pptemaa.logistics.driver;

public record Driver(long id, String name, String phone, String licenseNumber,
                     String car, String status) {
}
