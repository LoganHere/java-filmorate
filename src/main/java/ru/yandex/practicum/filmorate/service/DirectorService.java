package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorById(Long id) {
        Optional<Director> optionalDirector = directorStorage.getDirectorById(id);
        return optionalDirector.orElseThrow(() -> new NotFoundException("Режиссёр под ID " + id + " не найден"));
    }

    public Director updateDirector(Director director) {
        // проверка на id null
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(Long id) {
        directorStorage.deleteDirectorById(id);
    }
}
