package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

//Комментарии остались на случай, если не будут проходиться тесты postman на github
@Slf4j
@Repository("jdbcFilmStorage")
public class JdbcFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Autowired
    public JdbcFilmStorage(JdbcTemplate jdbcTemplate, FilmMapper filmMapper,
                           GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = filmMapper;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film addFilm(Film film) { // учесть добавление director
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
//            if (film.getDirector() != null) {
//                ps.setInt(6, film.getDirector().getId());
//            } else {
//                ps.setNull(6, Types.INTEGER);
//            }
            return ps;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        updateFilmGenres(filmId, film.getGenres());
        updateFilmDirectors(filmId, film.getDirector());

        log.debug("Добавлен фильм с id: {}", filmId);
        return getFilmById(filmId).orElseThrow();
    }

    @Override
    public Film updateFilm(Film film) { // учесть добавление director
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
//                film.getDirector() != null ? film.getDirector().getId() : null,
                film.getId()
        );

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());
        updateFilmGenres(film.getId(), film.getGenres());

        String deleteDirectorsSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorsSql, film.getId());
        updateFilmDirectors(film.getId(), film.getDirector());

        log.debug("Обновлен фильм с id: {}", film.getId());
        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        loadFilmDetails(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        loadFilmDetails(films);
        return Optional.of(films.get(0));
    }

    @Override
    public void deleteFilm(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Удален фильм с id: {}", id);
    }

    @Override
    public boolean containsFilm(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        loadFilmDetails(films);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Override
    public boolean existsLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public Map<Long, List<Long>> getLikesForFilms(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + placeholders + ")";
        Map<Long, List<Long>> likesMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            likesMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(userId);
        }, filmIds.toArray());
        return likesMap;
    }

    @Override
    public List<Film> getLikedFilmsByUser(Long userId) {
        String sql = """
                SELECT *
                FROM films
                WHERE id IN (SELECT film_id
                FROM film_likes
                WHERE user_id = ?);
                """;
        List<Film> films = jdbcTemplate.query(sql, filmMapper, userId);
        loadFilmDetails(films);
        return films;
    }

    @Override
    public List<Film> getAllDirectorFilmsSortedByLikes(int directorId) {
        String sql = """
                SELECT
                    f.ID,
                    f.NAME,
                    f.DESCRIPTION,
                    f.RELEASE_DATE,
                    f.DURATION,
                    f.MPA_ID,
                    d.ID AS director_id,
                    d.NAME AS director_name
                FROM FILMS f
                JOIN FILM_DIRECTORS fd ON fd.FILM_ID = f.ID
                JOIN DIRECTORS d ON d.ID = fd.DIRECTOR_ID
                LEFT JOIN (
                    SELECT FILM_ID, COUNT(*) AS likes_count
                    FROM FILM_LIKES
                    GROUP BY FILM_ID
                ) fl ON f.ID = fl.FILM_ID
                WHERE d.ID = ?
                GROUP BY f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, d.ID, d.NAME
                ORDER BY COALESCE(fl.likes_count, 0) DESC;
                """;
        List<Film> films = jdbcTemplate.query(sql, filmMapper, directorId);
        loadFilmDetails(films);
        return films;
    }

    @Override
    public List<Film> getAllDirectorFilmsSortedByYear(int directorId) {
        String sql = """
                SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.duration, f.MPA_ID, d.ID AS director_id,
                d.NAME AS director_name
                FROM FILMS f
                JOIN FILM_DIRECTORS fd ON fd.FILM_ID =f.ID
                JOIN DIRECTORS d ON d.ID = fd.DIRECTOR_ID
                WHERE d.ID = ?
                ORDER BY f.RELEASE_DATE DESC;
                """;
        List<Film> films = jdbcTemplate.query(sql, filmMapper, directorId);
        loadFilmDetails(films);
        return films;
    }

    private void loadFilmDetails(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, List<Genre>> genresMap = loadGenresForFilms(filmIds);
        Map<Long, List<Long>> likesMap = getLikesForFilms(filmIds);
        Map<Long, Mpa> mpaMap = loadMpaForFilms(filmIds);
        Map<Long, List<Director>> directorMap = loadDirectorForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(genresMap.getOrDefault(film.getId(), List.of())));
            film.setDirector(new LinkedHashSet<>(directorMap.getOrDefault(film.getId(), List.of())));
            film.setLikes(new HashSet<>(likesMap.getOrDefault(film.getId(), List.of())));
            if (mpaMap.containsKey(film.getId())) {
                film.setMpa(mpaMap.get(film.getId()));
            }
        }
    }

    private Map<Long, List<Genre>> loadGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY fg.film_id, g.id";

        Map<Long, List<Genre>> genresMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());

        return genresMap;
    }

    private Map<Long, List<Director>> loadDirectorForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
//        String sql = "SELECT f.id AS film_id, d.id AS director_id, d.name AS director_name " +
//                "FROM films f " +
//                "LEFT JOIN directors d ON f.director_id = d.id " +
//                "WHERE f.id IN (" + placeholders + ")";

        String sql = "SELECT fd.film_id, d.id, d.name FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id IN (" + placeholders + ") " +
                "ORDER BY fd.film_id, d.id";

        Map<Long, List<Director>> directorsMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Director director = new Director(rs.getInt("id"), rs.getString("name"));
            directorsMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(director);
        }, filmIds.toArray());

        return directorsMap;
    }

    private Map<Long, Mpa> loadMpaForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = filmIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT f.id AS film_id, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id IN (" + placeholders + ")";

        Map<Long, Mpa> mpaMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa(mpaId, rs.getString("mpa_name"));
                mpaMap.put(filmId, mpa);
            }
        }, filmIds.toArray());

        return mpaMap;
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = genres.stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void updateFilmDirectors(long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = directors.stream()
                .sorted(Comparator.comparingInt(Director::getId))
                .map(director -> new Object[]{filmId, director.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}