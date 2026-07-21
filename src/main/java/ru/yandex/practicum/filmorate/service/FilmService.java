package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        log.debug("Поиск фильма с id: {}", id);
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }

    public Film addFilm(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.containsFilm(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Long id) {
        if (!filmStorage.containsFilm(id)) {
            log.warn("Попытка удалить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        filmStorage.deleteFilm(id);
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

        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, filmId, userId);
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

        String deleteSql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteSql, filmId, userId);
        film.removeLike(userId);

        log.info("Пользователь {} убрал лайк с фильма {}. Осталось лайков: {}",
                userId, filmId, film.getLikesCount());
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.warn("Запрошено невалидное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }
        log.debug("Запрос на получение {} популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }

    private void validateMpa(Mpa mpa) {
        if (mpa != null) {
            try {
                Mpa.fromId(mpa.getId());
            } catch (IllegalArgumentException e) {
                log.warn("Несуществующий MPA: {}", mpa.getId());
                throw new NotFoundException("MPA с id " + mpa.getId() + " не найден");
            }
        }
    }

    private void validateGenres(java.util.Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                try {
                    Genre.fromId(genre.getId());
                } catch (IllegalArgumentException e) {
                    log.warn("Несуществующий жанр: {}", genre.getId());
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
    }
}