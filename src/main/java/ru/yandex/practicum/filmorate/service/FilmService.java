package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addLike(Long filmId, Long userId) {
        log.debug("Начало операции добавления лайка: фильм={}, пользователь={}", filmId, userId);
        Film film = getFilmById(filmId);

        if (!userStorage.containsUser(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (film.isLikedBy(userId)) {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.addLike(userId);
        log.info("Пользователь {} поставил лайк фильму {}. Всего лайков: {}",
                userId, filmId, film.getLikesCount());
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        log.debug("Начало операции удаления лайка: фильм={}, пользователь={}", filmId, userId);
        Film film = getFilmById(filmId);

        if (!userStorage.containsUser(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (!film.isLikedBy(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        film.removeLike(userId);
        log.info("Пользователь {} убрал лайк с фильма {}. Осталось лайков: {}",
                userId, filmId, film.getLikesCount());
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Запрос на получение {} популярных фильмов", count);
        List<Film> allFilms = filmStorage.getAllFilms();
        log.debug("Всего фильмов в хранилище: {}", allFilms.size());

        List<Film> popularFilms = allFilms.stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Возвращено {} популярных фильмов", popularFilms.size());
        if (!popularFilms.isEmpty()) {
            Film topFilm = popularFilms.get(0);
            log.debug("Самый популярный фильм: '{}' с {} лайками",
                    topFilm.getName(), topFilm.getLikesCount());
        }
        return popularFilms;
    }

    private Film getFilmById(Long id) {
        log.debug("Поиск фильма с id: {}", id);
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }
}