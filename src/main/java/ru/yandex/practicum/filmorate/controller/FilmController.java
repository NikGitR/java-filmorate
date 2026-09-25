package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate FIRST_FILM_DATE =
            LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new LinkedHashMap<>();
    private int nextId = 1;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validate(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        validate(film);

        if (film.getId() == null || !films.containsKey(film.getId())) {
            String message = "Фильм с id=" + film.getId() + " не найден";
            log.warn(message);
            throw new NotFoundException(message);
        }

        films.put(film.getId(), film);

        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            validationError("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            validationError("Описание фильма не должно превышать 200 символов");
        }

        if (film.getReleaseDate() == null) {
            validationError("Необходимо указать дату релиза");
        }

        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            validationError(
                    "Дата релиза не может быть раньше " + FIRST_FILM_DATE
            );
        }

        if (film.getDuration() <= 0) {
            validationError("Продолжительность фильма должна быть положительной");
        }
    }

    private void validationError(String message) {
        log.warn("Ошибка валидации фильма: {}", message);
        throw new ValidationException(message);
    }
}
