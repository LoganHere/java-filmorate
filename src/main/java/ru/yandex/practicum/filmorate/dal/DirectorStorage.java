package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director addDirector(Director director);

    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(int id);

    Director updateDirector(Director director);

    void deleteDirectorById(int id);

    boolean containsDirector(int id);
}