package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreDbStorage genreStorage;

    @GetMapping
    public List<Genre> findAll() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        return genreStorage.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Жанр не найден: " + id));
    }
}
