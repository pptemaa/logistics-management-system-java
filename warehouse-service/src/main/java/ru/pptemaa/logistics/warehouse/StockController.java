package ru.pptemaa.logistics.warehouse;

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
@RequestMapping("/api/stock")
public class StockController {
    private final StockRepository stock;

    public StockController(StockRepository stock) {
        this.stock = stock;
    }

    @PostMapping
    public ResponseEntity<StockItem> create(@Valid @RequestBody CreateStockItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stock.create(request));
    }

    @GetMapping
    public List<StockItem> list() {
        return stock.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockItem> get(@PathVariable long id) {
        return stock.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<String> duplicateProduct() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Product already registered");
    }
}
