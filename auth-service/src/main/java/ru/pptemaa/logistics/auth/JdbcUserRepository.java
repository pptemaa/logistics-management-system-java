package ru.pptemaa.logistics.auth;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserRepository {
    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"),
            rs.getString("email"), rs.getString("password_hash"),
            rs.getTimestamp("registered_at").toInstant());

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT * FROM users WHERE email = ?", USER_MAPPER, email)
                .stream().findFirst();
    }

    @Override
    public User save(String firstName, String lastName, String email, String passwordHash) {
        KeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO users (first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)",
                        new String[] {"id"});
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, email);
                statement.setString(4, passwordHash);
                return statement;
            }, keys);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateEmailException();
        }
        long id = keys.getKey().longValue();
        return jdbc.queryForObject("SELECT * FROM users WHERE id = ?", USER_MAPPER, id);
    }
}
