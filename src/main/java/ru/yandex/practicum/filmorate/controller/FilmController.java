package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с id: {}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма: {}", film.getName());
        log.debug("Детали фильма: описание='{}', длительность={}, дата релиза={}",
                film.getDescription(), film.getDuration(), film.getReleaseDate());
        Film addedFilm = filmService.addFilm(film);
        log.info("Фильм успешно добавлен с id: {}", addedFilm.getId());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма с id: {}", film.getId());
        log.debug("Обновление фильма: новое имя='{}', описание='{}'",
                film.getName(), film.getDescription());
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long id) {
        log.info("Запрос на удаление фильма с id: {}", id);
        filmService.deleteFilm(id);
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