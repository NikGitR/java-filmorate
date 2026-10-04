package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, Date.valueOf(user.getBirthday()));

            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден"
            );
        }

        return user;
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        int deletedRows = jdbcTemplate.update(sql, id);

        if (deletedRows == 0) {
            throw new NotFoundException(
                    "Пользователь с id=" + id + " не найден"
            );
        }
    }

    public Optional<User> findUserById(long id) {
        String sql = """
            SELECT id, email, login, name, birthday
            FROM users
            WHERE id = ?
            """;

        return jdbcTemplate.query(sql, USER_MAPPER, id)
                .stream()
                .findFirst();
    }

    @Override
    public User getById(long id) {
        return findUserById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + id + " не найден"
                ));
    }

    @Override
    public Collection<User> getAll() {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String sql = """
            MERGE INTO friendships (user_id, friend_id, confirmed)
            KEY (user_id, friend_id)
            VALUES (?, ?, FALSE)
            """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String sql = """
            DELETE FROM friendships
            WHERE user_id = ? AND friend_id = ?
            """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        String sql = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
            FROM users u
            JOIN friendships f ON f.friend_id = u.id
            WHERE f.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(sql, USER_MAPPER, userId);
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        String sql = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
            FROM users u
            JOIN friendships f1 ON f1.friend_id = u.id
            JOIN friendships f2 ON f2.friend_id = u.id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(sql, USER_MAPPER, userId, otherId);
    }
}