package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = """
                SELECT id, name
                FROM mpa_ratings
                ORDER BY id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new Mpa(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
        );
    }

    public Optional<Mpa> findById(int id) {
        String sql = """
                SELECT id, name
                FROM mpa_ratings
                WHERE id = ?
                """;

        List<Mpa> ratings = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new Mpa(
                                rs.getInt("id"),
                                rs.getString("name")
                        ),
                id
        );

        return ratings.stream().findFirst();
    }
}
