package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreService genreService, MpaService mpaService,
                       EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.eventService = eventService;
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
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.containsFilm(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateFilm(film);
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

        if (!userStorage.containsUser(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (filmStorage.existsLike(filmId, userId)) {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        filmStorage.addLike(filmId, userId);
        Film film = getFilmById(filmId);

        log.info("Пользователь {} поставил лайк фильму {}. Всего лайков: {}",
                userId, filmId, film.getLikesCount());

        eventService.saveEvent(userId, filmId, EventType.LIKE, Operation.ADD);

        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        log.debug("Начало операции удаления лайка: фильм={}, пользователь={}", filmId, userId);

        if (!userStorage.containsUser(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (!filmStorage.existsLike(filmId, userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        filmStorage.removeLike(filmId, userId);
        Film film = getFilmById(filmId);

        log.info("Пользователь {} убрал лайк с фильма {}. Осталось лайков: {}",
                userId, filmId, film.getLikesCount());

        eventService.saveEvent(userId, filmId, EventType.LIKE, Operation.REMOVE);

        return film;
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (count != null && count <= 0) {
            log.warn("Запрошено невалидное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }

        if (year != null && year < 1895) {
            log.warn("Запрошен невалидный год: {}", count);
            throw new ValidationException("Год не может быть раньше 1895");
        }

        if (genreId != null) {
            genreService.getGenreById(genreId);
        }

        log.debug("Запрос на получение {} популярных фильмов", count);
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getLikedFilmsByUser(Long userId) {
        log.debug("Начало операции получения списка понравившихся фильмов для пользователя с ID {}", userId);
        List<Film> likedFilms = filmStorage.getLikedFilmsByUser(userId);
        log.info("Найден список из {} пролайканных фильмов", likedFilms.size());
        return likedFilms;
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            log.warn("Поиск с пустым запросом – возвращаем пустой список");
            return Collections.emptyList();
        }
        List<Film> films = filmStorage.searchFilms(query, by);
        log.debug("Найдено {} фильмов по запросу '{}'", films.size(), query);
        return films;
    }

    public List<Film> getAllDirectorFilmsSortedByLikes(int directorId) {
        log.debug("Начало операции получения списка всех фильмов режиссёра с ID {}, сортированных по количеству лайков",
                directorId);
        List<Film> directorFilm = filmStorage.getAllDirectorFilmsSortedByLikes(directorId);
        log.info("Найден список из {} фильмов режиссёра, сортированных по количеству лайков", directorFilm.size());
        return directorFilm;
    }

    public List<Film> getAllDirectorFilmsSortedByYear(int directorId) {
        log.debug("Начало операции получения списка всех фильмов режиссёра с ID {}, сортированных по году релиза",
                directorId);
        List<Film> directorFilm = filmStorage.getAllDirectorFilmsSortedByYear(directorId);
        log.info("Найден список из {} фильмов режиссёра, сортированных по году релиза", directorFilm.size());
        return directorFilm;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        if (!userStorage.containsUser(userId)) {
            log.warn("Не удалось найти пользователя с id {}", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.containsUser(friendId)) {
            log.warn("Не удалось найти пользователя с id {}", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        List<Film> films = filmStorage.getCommonFilms(userId, friendId);
        log.info("Получен список из {} фильмов общих для пользователей с ID {} и {}", films.size(), userId, friendId);
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getMpa() != null) {
            mpaService.validateMpaExists(film.getMpa().getId());
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            genreService.validateGenresExist(genreIds);
        }
    }
}