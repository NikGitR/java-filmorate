package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(UserDbStorage.class)
@AutoConfigureTestDatabase
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateAndFindUser() {
        User created = createUser("first");

        Optional<User> saved =
                userStorage.findUserById(created.getId());

        assertThat(saved)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(created.getId());
                    assertThat(user.getEmail())
                            .isEqualTo("first@example.com");
                    assertThat(user.getLogin()).isEqualTo("first");
                });
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser("before");

        user.setEmail("after@example.com");
        user.setLogin("after");
        user.setName("Новое имя");
        user.setBirthday(LocalDate.of(1995, 5, 15));

        userStorage.update(user);

        User saved = userStorage.getById(user.getId());

        assertThat(saved.getEmail()).isEqualTo("after@example.com");
        assertThat(saved.getLogin()).isEqualTo("after");
        assertThat(saved.getName()).isEqualTo("Новое имя");
        assertThat(saved.getBirthday())
                .isEqualTo(LocalDate.of(1995, 5, 15));
    }

    @Test
    void shouldReturnAllUsers() {
        User first = createUser("first");
        User second = createUser("second");

        assertThat(userStorage.getAll())
                .extracting(User::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void shouldDeleteUser() {
        User user = createUser("deleted");

        userStorage.delete(user.getId());

        assertThat(userStorage.findUserById(user.getId()))
                .isEmpty();

        assertThatThrownBy(() ->
                userStorage.getById(user.getId())
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddFriendOnlyInOneDirection() {
        User user = createUser("user");
        User friend = createUser("friend");

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());

        assertThat(userStorage.getFriends(friend.getId()))
                .isEmpty();
    }

    @Test
    void shouldDeleteFriend() {
        User user = createUser("user");
        User friend = createUser("friend");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .isEmpty();
    }

    @Test
    void shouldReturnCommonFriends() {
        User first = createUser("first");
        User second = createUser("second");
        User common = createUser("common");
        User onlyFirst = createUser("onlyFirst");

        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(first.getId(), onlyFirst.getId());
        userStorage.addFriend(second.getId(), common.getId());

        assertThat(userStorage.getCommonFriends(
                first.getId(),
                second.getId()
        ))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    private User createUser(String suffix) {
        User user = new User();
        user.setEmail(suffix + "@example.com");
        user.setLogin(suffix);
        user.setName("Имя " + suffix);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        return userStorage.create(user);
    }
}