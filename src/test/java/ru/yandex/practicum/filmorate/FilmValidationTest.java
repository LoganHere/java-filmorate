package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private Validator validator;
    private Film film;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        film = new Film();
        film.setName("Начало");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
    }

    @Test
    void shouldCreateValidFilm() {
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotAllowBlankName() {
        film.setName("");
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotAllowDescriptionLongerThan200() {
        film.setDescription("a".repeat(201));
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotAllowReleaseDateBeforeMinDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldAllowReleaseDateExactlyMinDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotAllowZeroOrNegativeDuration() {
        film.setDuration(0);
        assertFalse(validator.validate(film).isEmpty());

        film.setDuration(-10);
        assertFalse(validator.validate(film).isEmpty());
    }
}