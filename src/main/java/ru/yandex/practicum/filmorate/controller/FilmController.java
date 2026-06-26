package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        List<Film> films = filmStorage.getAllFilms();
        log.debug("Возвращено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с id: {}", id);
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
        log.debug("Фильм с id {} успешно получен: {}", id, film.getName());
        return film;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма: {}", film.getName());
        log.debug("Детали фильма: описание='{}', длительность={}, дата релиза={}",
                film.getDescription(), film.getDuration(), film.getReleaseDate());
        Film addedFilm = filmStorage.addFilm(film);
        log.info("Фильм успешно добавлен с id: {}", addedFilm.getId());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма с id: {}", film.getId());
        if (!filmStorage.containsFilm(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        log.debug("Обновление фильма: новое имя='{}', описание='{}'",
                film.getName(), film.getDescription());
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long id) {
        log.info("Запрос на удаление фильма с id: {}", id);
        if (!filmStorage.containsFilm(id)) {
            log.warn("Попытка удалить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        filmStorage.deleteFilm(id);
        log.info("Фильм с id {} успешно удален", id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        Film film = filmService.addLike(id, userId);
        log.info("Лайк успешно добавлен. Текущее количество лайков у фильма {}: {}",
                id, film.getLikesCount());
        return film;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка у фильма {} от пользователя {}", id, userId);
        Film film = filmService.removeLike(id, userId);
        log.info("Лайк успешно удален. Текущее количество лайков у фильма {}: {}",
                id, film.getLikesCount());
        return film;
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.debug("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}