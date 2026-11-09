package ru.pptemaa.logistics.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public Order create(CreateOrderRequest request) {
        List<Order.Item> items = request.items().stream()
                .map(item -> new Order.Item(item.productId(), item.productName().trim(),
                        item.unitPrice(), item.quantity(),
                        item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))))
                .toList();
        BigDecimal total = items.stream().map(Order.Item::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long id = orders.insertOrder(request.userId(), request.deliveryAddress().trim(), total);
        items.forEach(item -> orders.insertItem(id, item));
        return orders.findById(id).orElseThrow();
    }
}
