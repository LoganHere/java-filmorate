package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<String, FriendshipStatus> friendshipStatuses = new HashMap<>();

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
        friendshipStatuses.entrySet().removeIf(entry -> {
            String[] ids = entry.getKey().split("_");
            return ids[0].equals(String.valueOf(id)) || ids[1].equals(String.valueOf(id));
        });
        userStorage.deleteUser(id);
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Запрос на добавление в друзья: пользователь={}, друг={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья. userId: {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        String requestKey = userId + "_" + friendId;
        String reverseKey = friendId + "_" + userId;

        if (friendshipStatuses.getOrDefault(requestKey, null) == FriendshipStatus.CONFIRMED ||
                friendshipStatuses.getOrDefault(reverseKey, null) == FriendshipStatus.CONFIRMED) {
            log.debug("Пользователи {} и {} уже являются друзьями", userId, friendId);
            return user;
        }

        if (friendshipStatuses.containsKey(reverseKey) &&
                friendshipStatuses.get(reverseKey) == FriendshipStatus.PENDING) {

            friendshipStatuses.put(reverseKey, FriendshipStatus.CONFIRMED);
            friendshipStatuses.put(requestKey, FriendshipStatus.CONFIRMED);

            user.addFriend(friendId);
            friend.addFriend(userId);

            log.info("Пользователь {} принял запрос в друзья от {}. Теперь они друзья!", friendId, userId);
            return user;
        }

        friendshipStatuses.put(requestKey, FriendshipStatus.PENDING);
        log.info("Пользователь {} отправил запрос на дружбу пользователю {}. Статус: {}", userId, friendId, FriendshipStatus.PENDING);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Запрос на удаление из друзей: пользователь={}, друг={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        String requestKey = userId + "_" + friendId;
        String reverseKey = friendId + "_" + userId;

        if (!friendshipStatuses.containsKey(requestKey) && !friendshipStatuses.containsKey(reverseKey)) {
            log.warn("Связи между {} и {} не найдено", userId, friendId);
            return user;
        }

        friendshipStatuses.remove(requestKey);
        friendshipStatuses.remove(reverseKey);

        if (user.isFriend(friendId)) {
            user.removeFriend(friendId);
            friend.removeFriend(userId);
        }

        log.info("Пользователь {} удалил из друзей/отклонил запрос пользователя {}", userId, friendId);
        return user;
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос списка друзей для пользователя {}", userId);
        User user = getUserById(userId);
        Set<Long> friendIds = user.getFriends();

        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            userStorage.getUserById(friendId).ifPresent(friends::add);
        }
        return friends;
    }

    public List<User> getFriendsByStatus(Long userId, FriendshipStatus status) {
        log.debug("Запрос друзей со статусом {} для пользователя {}", status, userId);
        getUserById(userId);

        return friendshipStatuses.entrySet().stream()
                .filter(entry -> {
                    String[] ids = entry.getKey().split("_");
                    return ids[0].equals(String.valueOf(userId)) && entry.getValue() == status;
                })
                .map(entry -> {
                    Long friendId = Long.parseLong(entry.getKey().split("_")[1]);
                    return userStorage.getUserById(friendId).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Запрос общих друзей для пользователей {} и {}", userId, otherUserId);
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : userFriends) {
            if (otherUserFriends.contains(friendId)) {
                userStorage.getUserById(friendId).ifPresent(commonFriends::add);
            }
        }
        return commonFriends;
    }
}