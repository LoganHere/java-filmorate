package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;

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

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreService genreService, MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
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

    public List<Film> getLikedFilmsByUser(Long userId) {
        log.debug("Начало операции получения списка понравившихся фильмов для пользователя с ID {}", userId);
        List<Film> likedFilms = filmStorage.getLikedFilmsByUser(userId);
        log.info("Найден список из {} пролайканных фильмов", likedFilms.size());
        return likedFilms;
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