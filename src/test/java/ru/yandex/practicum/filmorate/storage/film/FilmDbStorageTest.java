package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        FilmDbStorage.class,
        UserDbStorage.class
})
@AutoConfigureTestDatabase
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    FilmDbStorageTest(
            FilmDbStorage filmStorage,
            UserDbStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateAndFindFilm() {
        Film created = filmStorage.create(
                newFilm("Первый фильм", 1, 1, 2)
        );

        Film saved = filmStorage.getById(created.getId());

        assertThat(saved.getId()).isEqualTo(created.getId());
        assertThat(saved.getName()).isEqualTo("Первый фильм");
        assertThat(saved.getDescription())
                .isEqualTo("Описание: Первый фильм");
        assertThat(saved.getDuration()).isEqualTo(120);

        assertThat(saved.getMpa().getId()).isEqualTo(1);
        assertThat(saved.getMpa().getName()).isEqualTo("G");

        assertThat(saved.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    @Test
    void shouldUpdateFilmAndReplaceGenres() {
        Film film = filmStorage.create(
                newFilm("До обновления", 1, 1, 2)
        );

        film.setName("После обновления");
        film.setDescription("Новое описание");
        film.setDuration(150);
        film.setMpa(new Mpa(2, null));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(3, null));
        film.setGenres(genres);

        Film updated = filmStorage.update(film);

        assertThat(updated.getName())
                .isEqualTo("После обновления");
        assertThat(updated.getDescription())
                .isEqualTo("Новое описание");
        assertThat(updated.getDuration()).isEqualTo(150);
        assertThat(updated.getMpa().getId()).isEqualTo(2);
        assertThat(updated.getMpa().getName()).isEqualTo("PG");

        assertThat(updated.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void shouldReturnAllFilms() {
        Film first = filmStorage.create(
                newFilm("Первый", 1, 1)
        );
        Film second = filmStorage.create(
                newFilm("Второй", 2, 2)
        );

        assertThat(filmStorage.getAll())
                .extracting(Film::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void shouldDeleteFilm() {
        Film film = filmStorage.create(
                newFilm("Удаляемый фильм", 1, 1)
        );

        filmStorage.delete(film.getId());

        assertThatThrownBy(() ->
                filmStorage.getById(film.getId())
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddAndDeleteLike() {
        Film film = filmStorage.create(
                newFilm("Фильм с лайком", 1, 1)
        );
        User user = createUser("viewer");

        filmStorage.addLike(film.getId(), user.getId());

        List<Film> afterAdding =
                filmStorage.getPopularFilms(1);

        assertThat(afterAdding.get(0).getLikes())
                .containsExactly(user.getId());

        filmStorage.deleteLike(film.getId(), user.getId());

        List<Film> afterDeleting =
                filmStorage.getPopularFilms(1);

        assertThat(afterDeleting.get(0).getLikes())
                .isEmpty();
    }

    @Test
    void shouldReturnFilmsOrderedByLikes() {
        Film first = filmStorage.create(
                newFilm("Первый", 1, 1)
        );
        Film second = filmStorage.create(
                newFilm("Второй", 1, 2)
        );

        User firstUser = createUser("firstUser");
        User secondUser = createUser("secondUser");

        filmStorage.addLike(first.getId(), firstUser.getId());

        filmStorage.addLike(second.getId(), firstUser.getId());
        filmStorage.addLike(second.getId(), secondUser.getId());

        List<Film> popular = filmStorage.getPopularFilms(2);

        assertThat(popular)
                .extracting(Film::getId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    void shouldRespectPopularFilmsLimit() {
        Film first = filmStorage.create(
                newFilm("Первый", 1, 1)
        );
        Film second = filmStorage.create(
                newFilm("Второй", 1, 2)
        );

        User user = createUser("viewer");

        filmStorage.addLike(second.getId(), user.getId());

        List<Film> popular = filmStorage.getPopularFilms(1);

        assertThat(popular)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(second.getId());
    }

    private Film newFilm(
            String name,
            int mpaId,
            int... genreIds
    ) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание: " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(mpaId, null));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();

        for (int genreId : genreIds) {
            genres.add(new Genre(genreId, null));
        }

        film.setGenres(genres);
        return film;
    }

    private User createUser(String suffix) {
        User user = new User();
        user.setEmail(suffix + "@example.com");
        user.setLogin(suffix);
        user.setName("Имя " + suffix);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        return userStorage.create(user);
    }
}
