package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcFilmStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcFilmStorage filmStorage;
    private JdbcUserStorage userStorage;

    @BeforeEach
    void setUp() {
        GenreStorage genreStorage = new JdbcGenreStorage(jdbcTemplate);
        MpaStorage mpaStorage = new JdbcMpaStorage(jdbcTemplate);

        filmStorage = new JdbcFilmStorage(jdbcTemplate, new FilmMapper(), genreStorage, mpaStorage);
        userStorage = new JdbcUserStorage(jdbcTemplate, new UserMapper());
    }

    @Test
    void addFilm_ShouldReturnFilmWithGeneratedId() {
        Film film = createTestFilm();

        Film saved = filmStorage.addFilm(film);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("Матрица");
        assertThat(saved.getDuration()).isEqualTo(136);
        assertThat(saved.getMpa().getName()).isEqualTo("PG-13");
    }

    @Test
    void updateFilm_ShouldUpdateExistingFilm() {
        Film saved = filmStorage.addFilm(createTestFilm());
        saved.setName("Новое название");

        Film updated = filmStorage.updateFilm(saved);

        assertThat(updated.getName()).isEqualTo("Новое название");
    }

    @Test
    void getAllFilms_ShouldReturnListOfFilms() {
        filmStorage.addFilm(createTestFilm());
        filmStorage.addFilm(createTestFilm2());

        List<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void getFilmById_ShouldReturnFilm() {
        Film saved = filmStorage.addFilm(createTestFilm());

        Optional<Film> found = filmStorage.getFilmById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Матрица");
    }

    @Test
    void getFilmById_ShouldReturnEmptyForNotFound() {
        Optional<Film> found = filmStorage.getFilmById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteFilm_ShouldRemoveFilm() {
        Film saved = filmStorage.addFilm(createTestFilm());

        filmStorage.deleteFilm(saved.getId());

        assertThat(filmStorage.getFilmById(saved.getId())).isEmpty();
    }

    @Test
    void containsFilm_ShouldReturnTrueIfExists() {
        Film saved = filmStorage.addFilm(createTestFilm());

        boolean exists = filmStorage.containsFilm(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void getPopularFilms_ShouldReturnSortedByLikes() {
        User user = createTestUser();
        User savedUser = userStorage.addUser(user);

        Film film1 = filmStorage.addFilm(createTestFilm());
        Film film2 = filmStorage.addFilm(createTestFilm2());

        filmStorage.addLike(film1.getId(), savedUser.getId());

        List<Film> popular = filmStorage.getPopularFilms(2, null, null);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Научно-фантастический фильм");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        film.setMpa(new Mpa(3, "PG-13"));
        return film;
    }

    private Film createTestFilm2() {
        Film film = new Film();
        film.setName("Властелин колец");
        film.setDescription("Эпическое фэнтези");
        film.setReleaseDate(LocalDate.of(2001, 12, 19));
        film.setDuration(178);
        film.setMpa(new Mpa(3, "PG-13"));
        return film;
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Тест");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}