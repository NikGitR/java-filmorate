package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> findAll() {
        String sql = """
                SELECT id, name
                FROM genres
                ORDER BY id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new Genre(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
        );
    }

    public Optional<Genre> findById(int id) {
        String sql = """
                SELECT id, name
                FROM genres
                WHERE id = ?
                """;

        List<Genre> genres = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new Genre(
                                rs.getInt("id"),
                                rs.getString("name")
                        ),
                id
        );

        return genres.stream().findFirst();
    }
}
