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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user);
        replaceEmptyName(user);

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        validate(user);

        if (user.getId() == null || !users.containsKey(user.getId())) {
            String message = "Пользователь с id=" + user.getId() + " не найден";
            log.warn(message);
            throw new NotFoundException(message);
        }

        replaceEmptyName(user);
        users.put(user.getId(), user);

        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    private void validate(User user) {
        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {
            validationError("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            validationError("Логин не может быть пустым");
        }

        boolean containsWhitespace = user.getLogin()
                .chars()
                .anyMatch(Character::isWhitespace);

        if (containsWhitespace) {
            validationError("Логин не должен содержать пробелы");
        }

        if (user.getBirthday() == null) {
            validationError("Необходимо указать дату рождения");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            validationError("Дата рождения не может быть в будущем");
        }
    }

    private void replaceEmptyName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validationError(String message) {
        log.warn("Ошибка валидации пользователя: {}", message);
        throw new ValidationException(message);
    }
}
