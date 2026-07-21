package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        log.debug("Поиск пользователя с id: {}", id);
        return userStorage.getUserById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    public User createUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.containsUser(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя с id: {}", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return userStorage.updateUser(user);
    }

    public void deleteUser(Long id) {
        if (!userStorage.containsUser(id)) {
            log.warn("Попытка удалить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        String deleteFriendshipsSql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFriendshipsSql, id, id);

        userStorage.deleteUser(id);
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Запрос на добавление в друзья: пользователь={}, друг={}", userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья. userId: {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        getUserById(userId);
        getUserById(friendId);

        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            log.debug("Пользователи {} и {} уже являются друзьями", userId, friendId);
            return getUserById(userId);
        }

        String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.CONFIRMED.name());

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        return getUserById(userId);
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Запрос на удаление из друзей: пользователь={}, друг={}", userId, friendId);

        getUserById(userId);
        getUserById(friendId);

        String deleteSql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(deleteSql, userId, friendId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        return getUserById(userId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос списка друзей для пользователя {}", userId);
        getUserById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";

        return jdbcTemplate.query(sql, new UserMapper(), userId);
    }

    public List<User> getFriendsByStatus(Long userId, FriendshipStatus status) {
        log.debug("Запрос друзей со статусом {} для пользователя {}", status, userId);
        getUserById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = ?";

        return jdbcTemplate.query(sql, new UserMapper(), userId, status.name());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Запрос общих друзей для пользователей {} и {}", userId, otherUserId);
        getUserById(userId);
        getUserById(otherUserId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";

        return jdbcTemplate.query(sql, new UserMapper(), userId, otherUserId);
    }
}