package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Set<Long>> friends = new HashMap<>();

    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);

        friends.put(user.getId(), new HashSet<>());

        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден"
            );
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(long id) {
        getById(id);

        users.remove(id);

        friends.remove(id);

        friends.values().forEach(friendIds -> friendIds.remove(id));
    }

    @Override
    public User getById(long id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException(
                    "Пользователь с id=" + id + " не найден"
            );
        }

        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void addFriend(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        friends.get(userId).add(friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        friends.get(userId).remove(friendId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        getById(userId);

        return friends.get(userId).stream()
                .sorted()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        getById(userId);
        getById(otherId);

        Set<Long> commonFriendIds = new HashSet<>(friends.get(userId));
        commonFriendIds.retainAll(friends.get(otherId));

        return commonFriendIds.stream()
                .sorted()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}