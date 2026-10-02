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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserStorage userStorage;

    @MockitoBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        when(userStorage.create(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        user = new User(
                null,
                "user@example.com",
                "userLogin",
                "Иван",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void shouldAcceptValidUser() throws Exception {
        createUser(user)
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectEmailWithoutAtSymbol() throws Exception {
        user.setEmail("incorrect-email");

        createUser(user)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyEmail() throws Exception {
        user.setEmail(" ");

        createUser(user)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyLogin() throws Exception {
        user.setLogin(" ");

        createUser(user)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLoginWithSpaces() throws Exception {
        user.setLogin("user login");

        createUser(user)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFutureBirthday() throws Exception {
        user.setBirthday(LocalDate.now().plusDays(1));

        createUser(user)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptBirthdayToday() throws Exception {
        user.setBirthday(LocalDate.now());

        createUser(user)
                .andExpect(status().isOk());
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() throws Exception {
        user.setName("");

        createUser(user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    private ResultActions createUser(User user) throws Exception {
        return mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));
    }
}