package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    private UserController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new UserController();

        user = new User(
                null,
                "user@example.com",
                "userLogin",
                "Иван",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void shouldAcceptValidUser() {
        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void shouldRejectEmailWithoutAtSymbol() {
        user.setEmail("incorrect-email");

        assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
    }

    @Test
    void shouldRejectEmptyEmail() {
        user.setEmail(" ");

        assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
    }

    @Test
    void shouldRejectEmptyLogin() {
        user.setLogin(" ");

        assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        user.setLogin("user login");

        assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
    }

    @Test
    void shouldRejectFutureBirthday() {
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
    }

    @Test
    void shouldAcceptBirthdayToday() {
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        user.setName("");

        User createdUser = controller.create(user);

        assertEquals(user.getLogin(), createdUser.getName());
    }
}