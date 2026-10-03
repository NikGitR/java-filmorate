package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_FILMS = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id AS mpa_id,
                   m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON m.id = f.mpa_id
            """;

    private static final RowMapper<Film> FILM_MAPPER = (rs, rowNum) -> {
        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        film.setMpa(new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        ));

        return film;
    };

    @Override
    @Transactional
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (
                    name,
                    description,
                    release_date,
                    duration,
                    mpa_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());

            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        saveGenres(film);

        return getById(id);
    }

    @Override
    @Transactional
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "Фильм с id=" + film.getId() + " не найден"
            );
        }

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        return getById(film.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM films WHERE id = ?";

        int deletedRows = jdbcTemplate.update(sql, id);

        if (deletedRows == 0) {
            throw new NotFoundException(
                    "Фильм с id=" + id + " не найден"
            );
        }
    }

    @Override
    public Film getById(long id) {
        String sql = SELECT_FILMS + """
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, FILM_MAPPER, id);

        Film film = films.stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Фильм с id=" + id + " не найден"
                ));

        film.setGenres(getFilmGenres(id));
        film.setLikes(getFilmLikes(id));

        return film;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = SELECT_FILMS + """
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(sql, FILM_MAPPER);

        films.forEach(film -> {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
        });

        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .forEach(genreId ->
                        jdbcTemplate.update(sql, film.getId(), genreId)
                );
    }

    private Set<Genre> getFilmGenres(long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                filmId
        );

        return new LinkedHashSet<>(genres);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = """
            MERGE INTO film_likes (film_id, user_id)
            KEY (film_id, user_id)
            VALUES (?, ?)
            """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String sql = """
            DELETE FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id AS mpa_id,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes_count
            FROM films f
            JOIN mpa_ratings m ON m.id = f.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.id
            GROUP BY f.id,
                     f.name,
                     f.description,
                     f.release_date,
                     f.duration,
                     m.id,
                     m.name
            ORDER BY likes_count DESC, f.id
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(
                sql,
                FILM_MAPPER,
                count
        );

        films.forEach(film -> {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
        });

        return films;
    }

    private Set<Long> getFilmLikes(long filmId) {
        String sql = """
            SELECT user_id
            FROM film_likes
            WHERE film_id = ?
            ORDER BY user_id
            """;

        return new LinkedHashSet<>(
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> rs.getLong("user_id"),
                        filmId
                )
        );
    }
}