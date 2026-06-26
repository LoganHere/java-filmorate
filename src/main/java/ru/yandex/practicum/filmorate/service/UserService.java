package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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
        userStorage.deleteUser(id);
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Начало операции добавления в друзья: пользователь={}, друг={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья. userId: {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (user.isFriend(friendId)) {
            log.debug("Пользователи {} и {} уже являются друзьями", userId, friendId);
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        log.info("Пользователь {} и {} стали друзьями. У пользователя {} теперь {} друзей, у пользователя {} - {} друзей",
                userId, friendId, userId, user.getFriends().size(), friendId, friend.getFriends().size());
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Начало операции удаления из друзей: пользователь={}, друг={}", userId, friendId);
        User user = getUserById(userId);
        getUserById(friendId);

        if (!user.isFriend(friendId)) {
            log.warn("Пользователь {} не является другом {}. Возвращаем успех без изменений", userId, friendId);
            return user;
        }

        User friend = getUserById(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);

        log.info("Пользователь {} и {} перестали быть друзьями. У пользователя {} теперь {} друзей, у пользователя {} - {} друзей",
                userId, friendId, userId, user.getFriends().size(), friendId, friend.getFriends().size());
        return user;
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос списка друзей для пользователя {}", userId);
        User user = getUserById(userId);
        Set<Long> friendIds = user.getFriends();
        log.debug("Пользователь {} имеет {} друзей", userId, friendIds.size());

        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            userStorage.getUserById(friendId).ifPresent(friends::add);
        }

        log.debug("Возвращено {} друзей для пользователя {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Запрос общих друзей для пользователей {} и {}", userId, otherUserId);
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        log.debug("У пользователя {} {} друзей, у пользователя {} {} друзей",
                userId, userFriends.size(), otherUserId, otherUserFriends.size());

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : userFriends) {
            if (otherUserFriends.contains(friendId)) {
                userStorage.getUserById(friendId).ifPresent(commonFriends::add);
            }
        }

        log.info("Найдено {} общих друзей у пользователей {} и {}",
                commonFriends.size(), userId, otherUserId);
        return commonFriends;
    }
}