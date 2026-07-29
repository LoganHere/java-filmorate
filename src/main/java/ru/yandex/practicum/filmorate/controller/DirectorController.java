package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director addDirector(@RequestBody Director director) {
        log.info("Запрос на добавление режиссёра: {}", director.getName());
        return directorService.addDirector(director);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        log.debug("Запрос на получение всех режиссёров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        log.debug("Запрос на получение режиссёра с id: {}", id);
        return directorService.getDirectorById(id);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        log.info("Запрос на обновление режиссёра с id: {}", director.getId());
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirectorById(@PathVariable int id) {
        log.info("Запрос на удаление режиссёра с id: {}", id);
        directorService.deleteDirectorById(id);
    }
}