package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(GenreDbStorage.class)
@AutoConfigureTestDatabase
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Autowired
    GenreDbStorageTest(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @Test
    void shouldReturnAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .hasSize(6)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void shouldFindGenreById() {
        assertThat(genreStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1);
                    assertThat(genre.getName())
                            .isEqualTo("Комедия");
                });
    }

    @Test
    void shouldReturnEmptyForUnknownGenre() {
        assertThat(genreStorage.findById(100))
                .isEmpty();
    }
}
