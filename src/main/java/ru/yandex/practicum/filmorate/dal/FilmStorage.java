package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    void deleteFilm(Long id);

    boolean containsFilm(Long id);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    boolean existsLike(Long filmId, Long userId);

    Map<Long, List<Long>> getLikesForFilms(List<Long> filmIds);

    List<Film> getLikedFilmsByUser(Long userId);

    List<Film> searchFilms(String query, String by);

    List<Film> getAllDirectorFilmsSortedByLikes(int directorId);

    List<Film> getAllDirectorFilmsSortedByYear(int directorId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getLikedFilmsByUsers(List<Long> userIds);
}