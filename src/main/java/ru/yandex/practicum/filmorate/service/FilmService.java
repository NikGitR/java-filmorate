package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().add(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count < 0) {
            throw new ValidationException(
                    "Количество фильмов должно быть указано и не может быть отрицательным"
            );
        }

        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(
                        (Film film) -> film.getLikes().size()
                ).reversed())
                .limit(count)
                .toList();
    }
}
