package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dal.MpaStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public Mpa getMpaById(int id) {
        log.debug("Поиск рейтинга с id: {}", id);
        return mpaStorage.getMpaById(id)
                .orElseThrow(() -> {
                    log.warn("Рейтинг с id {} не найден", id);
                    return new NotFoundException("Рейтинг с id " + id + " не найден");
                });
    }

    public void validateMpaExists(int id) {
        try {
            getMpaById(id);
        } catch (NotFoundException e) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
    }
}