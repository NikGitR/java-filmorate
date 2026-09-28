package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FilmStorage filmStorage;

    @MockitoBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        when(filmStorage.create(any(Film.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        film = new Film(
                null,
                "Интерстеллар",
                "Научно-фантастический фильм",
                LocalDate.of(2014, 11, 6),
                169
        );
    }

    @Test
    void shouldAcceptValidFilm() throws Exception {
        createFilm(film)
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectBlankName() throws Exception {
        film.setName(" ");

        createFilm(film)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters()
            throws Exception {
        film.setDescription("a".repeat(201));

        createFilm(film)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptDescriptionWith200Characters()
            throws Exception {
        film.setDescription("a".repeat(200));

        createFilm(film)
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectReleaseDateBeforeFirstFilm()
            throws Exception {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        createFilm(film)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptFirstFilmDate() throws Exception {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        createFilm(film)
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectZeroDuration() throws Exception {
        film.setDuration(0);

        createFilm(film)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNegativeDuration() throws Exception {
        film.setDuration(-1);

        createFilm(film)
                .andExpect(status().isBadRequest());
    }

    private ResultActions createFilm(Film film) throws Exception {
        return mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film)));
    }
}