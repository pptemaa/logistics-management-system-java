package ru.pptemaa.logistics.driver;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverRepository drivers;

    public DriverController(DriverRepository drivers) {
        this.drivers = drivers;
    }

    @PostMapping
    public ResponseEntity<Driver> create(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drivers.create(request));
    }

    @GetMapping
    public List<Driver> list() {
        return drivers.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> get(@PathVariable long id) {
        return drivers.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(DuplicateLicenseException.class)
    public ResponseEntity<String> duplicateLicense() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("License number already registered");
    }
}
