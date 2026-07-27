package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Map;

public interface LikeStorage {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    boolean exists(Long filmId, Long userId);

    Map<Long, List<Long>> getLikesForFilms(List<Long> filmIds);
}