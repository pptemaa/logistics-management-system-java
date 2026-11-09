package ru.pptemaa.logistics.order;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insertOrder(long userId, String address, BigDecimal total) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders (user_id, delivery_address, total_amount) VALUES (?, ?, ?)",
                    new String[] {"id"});
            statement.setLong(1, userId);
            statement.setString(2, address);
            statement.setBigDecimal(3, total);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    public void insertItem(long orderId, Order.Item item) {
        jdbc.update("INSERT INTO order_items "
                        + "(order_id, product_id, product_name, unit_price, quantity, line_total) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                orderId, item.productId(), item.productName(), item.unitPrice(),
                item.quantity(), item.lineTotal());
    }

    public Optional<Order> findById(long id) {
        List<Order> orders = jdbc.query("SELECT * FROM orders WHERE id = ?",
                (rs, rowNum) -> new Order(rs.getLong("id"), rs.getLong("user_id"),
                        rs.getString("status"), rs.getString("delivery_address"),
                        rs.getBigDecimal("total_amount"), (Long) rs.getObject("driver_id"),
                        rs.getTimestamp("created_at").toInstant(), List.of()), id);
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        List<Order.Item> items = jdbc.query(
                "SELECT * FROM order_items WHERE order_id = ? ORDER BY id",
                (rs, rowNum) -> new Order.Item(rs.getLong("product_id"),
                        rs.getString("product_name"), rs.getBigDecimal("unit_price"),
                        rs.getInt("quantity"), rs.getBigDecimal("line_total")), id);
        Order order = orders.getFirst();
        return Optional.of(new Order(order.id(), order.userId(), order.status(),
                order.deliveryAddress(), order.totalAmount(), order.driverId(),
                order.createdAt(), items));
    }
}
