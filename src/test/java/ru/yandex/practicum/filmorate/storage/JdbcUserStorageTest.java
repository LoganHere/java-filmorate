package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcUserStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcUserStorage userStorage;

    @BeforeEach
    void setUp() {
        FriendshipStorage friendshipStorage = new JdbcFriendshipStorage(jdbcTemplate);
        userStorage = new JdbcUserStorage(jdbcTemplate, new UserMapper(), friendshipStorage);
    }

    @Test
    void addUser_ShouldReturnUserWithGeneratedId() {
        User user = createTestUser();

        User saved = userStorage.addUser(user);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getEmail()).isEqualTo("test@mail.ru");
        assertThat(saved.getLogin()).isEqualTo("testuser");
        assertThat(saved.getName()).isEqualTo("Тест");
        assertThat(saved.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User user = userStorage.addUser(createTestUser());
        user.setName("Новое имя");

        User updated = userStorage.updateUser(user);

        assertThat(updated.getName()).isEqualTo("Новое имя");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        userStorage.addUser(createTestUser());
        userStorage.addUser(createTestUser2());

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void getUserById_ShouldReturnUser() {
        User saved = userStorage.addUser(createTestUser());

        Optional<User> found = userStorage.getUserById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    void getUserById_ShouldReturnEmptyForNotFound() {
        Optional<User> found = userStorage.getUserById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        User saved = userStorage.addUser(createTestUser());

        userStorage.deleteUser(saved.getId());

        assertThat(userStorage.getUserById(saved.getId())).isEmpty();
    }

    @Test
    void containsUser_ShouldReturnTrueIfExists() {
        User saved = userStorage.addUser(createTestUser());

        boolean exists = userStorage.containsUser(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void containsUser_ShouldReturnFalseIfNotExists() {
        boolean exists = userStorage.containsUser(999L);

        assertThat(exists).isFalse();
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Тест");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private User createTestUser2() {
        User user = new User();
        user.setEmail("test2@mail.ru");
        user.setLogin("testuser2");
        user.setName("Тест2");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}