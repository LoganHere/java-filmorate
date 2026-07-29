package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

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

    public Director getDirectorById(int id) {
        log.debug("Поиск режиссёра с id: {}", id);
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> {
                    log.warn("Режиссёр с id {} не найден", id);
                    return new NotFoundException("Режиссёр под ID " + id + " не найден");
                });
    }

    public Director updateDirector(Director director) {
        if (director.getId() == 0) {
            log.warn("Попытка обновить данные режиссёра без указания ID");
            throw new ValidationException("ID режиссёра должен быть указан!");
        }
        if (!directorStorage.containsDirector(director.getId())) {
            log.warn("Попытка обновить несуществующего режиссёра с id: {}", director.getId());
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
        }
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        if (!directorStorage.containsDirector(id)) {
            log.warn("Попытка удалить несуществующего режиссёра с id: {}", id);
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
        directorStorage.deleteDirectorById(id);
    }
}
