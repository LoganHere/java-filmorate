package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.dal.GenreStorage;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        log.debug("Поиск жанра с id: {}", id);
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр с id {} не найден", id);
                    return new NotFoundException("Жанр с id " + id + " не найден");
                });
    }

    public List<Genre> getGenresByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return genreStorage.getGenresByIds(ids);
    }

    public void validateGenresExist(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Genre> foundGenres = getGenresByIds(ids);
        if (foundGenres.size() != ids.size()) {
            log.warn("Не все жанры найдены. Запрошено: {}, найдено: {}", ids.size(), foundGenres.size());
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }
}