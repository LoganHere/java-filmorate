package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmService filmService;

    @Autowired
    public UserService(UserStorage userStorage, FilmService filmService) {
        this.userStorage = userStorage;
        this.filmService = filmService;
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
        userStorage.deleteAllFriendsByUserId(id);
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

        if (userStorage.existsFriend(userId, friendId)) {
            log.debug("Пользователи {} и {} уже являются друзьями", userId, friendId);
            return getUserById(userId);
        }

        userStorage.addFriend(userId, friendId, FriendshipStatus.CONFIRMED);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        return getUserById(userId);
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Запрос на удаление из друзей: пользователь={}, друг={}", userId, friendId);

        getUserById(userId);
        getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        return getUserById(userId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос списка друзей для пользователя {}", userId);
        getUserById(userId);

        List<Long> friendIds = userStorage.getFriendIds(userId);
        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            userStorage.getUserById(friendId).ifPresent(friends::add);
        }
        return friends;
    }

    public List<User> getFriendsByStatus(Long userId, FriendshipStatus status) {
        log.debug("Запрос друзей со статусом {} для пользователя {}", status, userId);
        getUserById(userId);

        List<Long> friendIds = userStorage.getFriendIdsByStatus(userId, status);
        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            userStorage.getUserById(friendId).ifPresent(friends::add);
        }
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Запрос общих друзей для пользователей {} и {}", userId, otherUserId);
        getUserById(userId);
        getUserById(otherUserId);

        Set<Long> userFriends = userStorage.getFriendIds(userId).stream().collect(Collectors.toSet());
        Set<Long> otherUserFriends = userStorage.getFriendIds(otherUserId).stream().collect(Collectors.toSet());

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : userFriends) {
            if (otherUserFriends.contains(friendId)) {
                userStorage.getUserById(friendId).ifPresent(commonFriends::add);
            }
        }
        return commonFriends;
    }

    public List<Film> getFilmsRecommendations(Long userId) {
        log.info("Начинается поиск пользователей для составления рекомендации.");
        if (!isExistsLikedFilms(userId)) {
            log.info("Пользователь с ID {} ещё не ставил like ни одному фильму", userId);
            return Collections.emptyList();
        }

        List<Long> otherUserIds = userStorage.getUserIdsWithMostLikedFilmsMatches(userId);
        if (otherUserIds.isEmpty()) {
            log.info("Не нашлось ни одного пользователя с пересечением по понравившимся фильмам.");
            return Collections.emptyList();
        }
        log.info("ID пользователей для составление рекомендации {}", otherUserIds);

        List<Film> filmsLikedByUser = filmService.getLikedFilmsByUser(userId);
        List<Film> recommendedFilms = new ArrayList<>();

        for (Long otherUserId : otherUserIds) {
            List<Film> filmsLikedByOtherUser = filmService.getLikedFilmsByUser(otherUserId);
            recommendedFilms = filmsLikedByOtherUser.stream()
                    .filter(film -> !filmsLikedByUser.contains(film))
                    .collect(Collectors.toList());

            if (!recommendedFilms.isEmpty()) {
                break;
            }
        }
        return recommendedFilms;
    }

    private boolean isExistsLikedFilms(Long userId) {
        log.info("Проверка существования записи в таблице film_likes для пользователя с ID {}", userId);
        return userStorage.isExistsLikedFilms(userId);
    }
}