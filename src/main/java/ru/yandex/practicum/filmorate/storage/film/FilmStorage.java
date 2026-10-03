package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    void delete(long id);

    Film getById(long id);

    Collection<Film> getAll();

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    List<Film> getPopularFilms(int count);
}