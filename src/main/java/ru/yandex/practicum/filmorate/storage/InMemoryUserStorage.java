package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User addUser(User user) {
        user.setId(++currentId);
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище с id: {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.debug("Пользователь обновлен в хранилище с id: {}", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
        log.debug("Пользователь удален из хранилища с id: {}", id);
    }

    @Override
    public boolean containsUser(Long id) {
        return users.containsKey(id);
    }
}