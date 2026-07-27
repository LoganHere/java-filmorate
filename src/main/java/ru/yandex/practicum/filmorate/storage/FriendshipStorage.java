package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;

public interface FriendshipStorage {
    void addFriend(Long userId, Long friendId, FriendshipStatus status);

    void removeFriend(Long userId, Long friendId);

    List<Long> getFriendIds(Long userId);

    List<Long> getFriendIdsByStatus(Long userId, FriendshipStatus status);

    boolean exists(Long userId, Long friendId);

    void deleteAllByUserId(Long userId);
}