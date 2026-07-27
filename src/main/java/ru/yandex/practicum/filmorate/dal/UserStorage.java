package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void deleteUser(Long id);

    boolean containsUser(Long id);

    void addFriend(Long userId, Long friendId, FriendshipStatus status);

    void removeFriend(Long userId, Long friendId);

    List<Long> getFriendIds(Long userId);

    List<Long> getFriendIdsByStatus(Long userId, FriendshipStatus status);

    boolean existsFriend(Long userId, Long friendId);

    void deleteAllFriendsByUserId(Long userId);
}