package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Collection;

@Service
public class UserService {

     private final UserStorage userStorage;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException(
                    "Пользователь не может добавить самого себя в друзья"
            );
        }

        userStorage.getById(userId);
        userStorage.getById(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);

        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        userStorage.getById(userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        userStorage.getById(userId);
        userStorage.getById(otherId);

        return userStorage.getCommonFriends(userId, otherId);
    }
}
