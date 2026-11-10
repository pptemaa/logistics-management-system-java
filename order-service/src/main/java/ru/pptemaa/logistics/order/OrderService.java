package ru.pptemaa.logistics.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.pptemaa.logistics.contracts.warehouse.ProductInfo;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final WarehouseCatalogClient warehouse;
    private final DriverDirectoryClient drivers;

    public OrderService(OrderRepository orders, WarehouseCatalogClient warehouse,
            DriverDirectoryClient drivers) {
        this.orders = orders;
        this.warehouse = warehouse;
        this.drivers = drivers;
    }

    @Transactional
    public Order create(CreateOrderRequest request) {
        List<Order.Item> items = request.items().stream()
                .map(item -> {
                    ProductInfo product = warehouse.getProduct(item.productId());
                    if (product.getAvailableQuantity() < item.quantity()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Insufficient stock");
                    }
                    BigDecimal price = BigDecimal.valueOf(product.getPriceCents(), 2);
                    return new Order.Item(item.productId(), product.getProductName(),
                            price, item.quantity(),
                            price.multiply(BigDecimal.valueOf(item.quantity())));
                })
                .toList();
        BigDecimal total = items.stream().map(Order.Item::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long id = orders.insertOrder(request.userId(), request.deliveryAddress().trim(), total);
        items.forEach(item -> orders.insertItem(id, item));
        return orders.findById(id).orElseThrow();
    }

    @Transactional
    public Order changeStatus(long id, UpdateOrderStatusRequest request) {
        Order current = orders.findById(id).orElse(null);
        if (current == null) {
            return null;
        }
        if (!current.status().canTransitionTo(request.status())
                || (request.status() == OrderStatus.ASSIGNED) != (request.driverId() != null)) {
            throw new InvalidOrderTransitionException();
        }
        if (request.status() == OrderStatus.ASSIGNED
                && !"available".equals(drivers.getDriver(request.driverId()).getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is not available");
        }
        if (!orders.updateStatus(id, current.status(), request.status(), request.driverId())) {
            throw new InvalidOrderTransitionException();
        }
        return orders.findById(id).orElseThrow();
    }
}
