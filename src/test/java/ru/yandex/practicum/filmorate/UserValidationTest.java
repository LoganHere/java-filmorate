package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldCreateValidUser() {
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotAllowBlankEmail() {
        user.setEmail("");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldRequireAtSymbolInEmail() {
        user.setEmail("userexample.com");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotAllowBlankLogin() {
        user.setLogin("");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotAllowSpacesInLogin() {
        user.setLogin("user 123");
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldAllowEmptyName() {
        user.setName("");
        assertTrue(validator.validate(user).isEmpty());
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void shouldNotAllowBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotAllowNullBirthday() {
        user.setBirthday(null);
        assertFalse(validator.validate(user).isEmpty());
    }
}