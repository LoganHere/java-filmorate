package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя с id: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя с логином: {}", user.getLogin());
        log.debug("Детали пользователя: email={}, имя={}, дата рождения={}",
                user.getEmail(), user.getName(), user.getBirthday());
        User createdUser = userService.createUser(user);
        log.info("Пользователь успешно создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя с id: {}", user.getId());
        log.debug("Обновление пользователя: новый логин='{}', email='{}'",
                user.getLogin(), user.getEmail());
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя с id: {}", id);
        userService.deleteUser(id);
        log.info("Пользователь с id {} успешно удален", id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на добавление в друзья пользователя {} к пользователю {}", friendId, id);
        User user = userService.addFriend(id, friendId);
        log.info("Пользователь {} и {} теперь друзья. У пользователя {} теперь {} друзей",
                id, friendId, id, user.getFriends().size());
        return user;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на удаление из друзей пользователя {} у пользователя {}", friendId, id);
        User user = userService.removeFriend(id, friendId);
        log.info("Пользователь {} и {} больше не друзья. У пользователя {} теперь {} друзей",
                id, friendId, id, user.getFriends().size());
        return user;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.debug("Запрос списка друзей для пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получение общих друзей пользователей {} и {}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.debug("Найдено {} общих друзей для пользователей {} и {}",
                commonFriends.size(), id, otherId);
        return commonFriends;
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getFilmsRecommendations(@PathVariable Long id) {
        log.info("Запрос на получение рекомендации по фильмам для пользователя с ID {}", id);
        List<Film> recommendedFilms = userService.getFilmsRecommendations(id);
        log.debug("Найдено {} рекомендованных фильмов для пользователя с ID {}", recommendedFilms.size(), id);
        return recommendedFilms;
    }
}