package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private FilmController controller;
    private Film film;

    @BeforeEach
    void setUp() {
        controller = new FilmController();

        film = new Film(
                null,
                "Интерстеллар",
                "Научно-фантастический фильм",
                LocalDate.of(2014, 11, 6),
                169
        );
    }

    @Test
    void shouldAcceptValidFilm() {
        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldRejectBlankName() {
        film.setName(" ");

        assertThrows(
                ValidationException.class,
                () -> controller.create(film)
        );
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        film.setDescription("a".repeat(201));

        assertThrows(
                ValidationException.class,
                () -> controller.create(film)
        );
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldRejectReleaseDateBeforeFirstFilm() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(
                ValidationException.class,
                () -> controller.create(film)
        );
    }

    @Test
    void shouldAcceptFirstFilmDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldRejectZeroDuration() {
        film.setDuration(0);

        assertThrows(
                ValidationException.class,
                () -> controller.create(film)
        );
    }

    @Test
    void shouldRejectNegativeDuration() {
        film.setDuration(-1);

        assertThrows(
                ValidationException.class,
                () -> controller.create(film)
        );
    }
}
