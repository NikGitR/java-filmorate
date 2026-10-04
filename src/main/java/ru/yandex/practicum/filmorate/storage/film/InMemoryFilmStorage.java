package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException(
                    "Фильм с id=" + film.getId() + " не найден"
            );
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(long id) {
        getById(id);
        films.remove(id);
    }

    @Override
    public Film getById(long id) {
        Film film = films.get(id);

        if (film == null) {
            throw new NotFoundException(
                    "Фильм с id=" + id + " не найден"
            );
        }

        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = getById(filmId);
        film.getLikes().add(userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        Film film = getById(filmId);
        film.getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted(
                        Comparator.comparingInt(
                                (Film film) -> film.getLikes().size()
                        ).reversed()
                )
                .limit(count)
                .toList();
    }
}