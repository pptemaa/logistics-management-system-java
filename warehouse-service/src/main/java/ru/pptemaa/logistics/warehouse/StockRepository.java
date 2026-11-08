package ru.pptemaa.logistics.warehouse;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class StockRepository {
    private static final RowMapper<StockItem> MAPPER = (rs, rowNum) -> new StockItem(
            rs.getLong("product_id"), rs.getString("product_name"), rs.getInt("quantity"),
            rs.getBigDecimal("price"), rs.getTimestamp("last_updated").toInstant());
    private final JdbcTemplate jdbc;

    public StockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StockItem create(CreateStockItemRequest request) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO warehouse_stock (product_name, quantity, price) VALUES (?, ?, ?)",
                        new String[] {"product_id"});
                statement.setString(1, request.productName().trim());
                statement.setInt(2, request.quantity());
                statement.setBigDecimal(3, request.price());
                return statement;
            }, keys);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateProductException();
        }
        return findById(keys.getKey().longValue()).orElseThrow();
    }

    public List<StockItem> findAll() {
        return jdbc.query("SELECT * FROM warehouse_stock ORDER BY product_id", MAPPER);
    }

    public Optional<StockItem> findById(long id) {
        return jdbc.query("SELECT * FROM warehouse_stock WHERE product_id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public boolean reserve(long id, int quantity) {
        return jdbc.update(
                "UPDATE warehouse_stock SET quantity = quantity - ?, last_updated = CURRENT_TIMESTAMP "
                        + "WHERE product_id = ? AND quantity >= ?",
                quantity, id, quantity) == 1;
    }

    public boolean replenish(long id, int quantity) {
        return jdbc.update(
                "UPDATE warehouse_stock SET quantity = quantity + ?, last_updated = CURRENT_TIMESTAMP "
                        + "WHERE product_id = ?",
                quantity, id) == 1;
    }
}
