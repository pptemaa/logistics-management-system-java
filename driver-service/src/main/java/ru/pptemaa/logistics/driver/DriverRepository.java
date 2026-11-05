package ru.pptemaa.logistics.driver;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class DriverRepository {
    private static final RowMapper<Driver> MAPPER = (rs, rowNum) -> new Driver(
            rs.getLong("id"), rs.getString("name"), rs.getString("phone"),
            rs.getString("license_number"), rs.getString("car"), rs.getString("status"));
    private final JdbcTemplate jdbc;

    public DriverRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Driver create(CreateDriverRequest request) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO drivers (name, phone, license_number, car) VALUES (?, ?, ?, ?)",
                        new String[] {"id"});
                statement.setString(1, request.name().trim());
                statement.setString(2, request.phone().trim());
                statement.setString(3, request.licenseNumber().trim());
                statement.setString(4, request.car().trim());
                return statement;
            }, keys);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateLicenseException();
        }
        return findById(keys.getKey().longValue()).orElseThrow();
    }

    public List<Driver> findAll() {
        return jdbc.query("SELECT * FROM drivers ORDER BY id", MAPPER);
    }

    public Optional<Driver> findById(long id) {
        return jdbc.query("SELECT * FROM drivers WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }
}
