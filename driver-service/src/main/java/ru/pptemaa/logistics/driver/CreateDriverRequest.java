package ru.pptemaa.logistics.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Size(max = 100) String licenseNumber,
        @NotBlank @Size(max = 255) String car
) {
}
