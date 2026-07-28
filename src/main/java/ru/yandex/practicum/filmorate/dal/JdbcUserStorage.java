package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("jdbcUserStorage")
public class JdbcUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Autowired
    public JdbcUserStorage(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(userId);

        log.debug("Добавлен пользователь с id: {}", userId);
        return getUserById(userId).orElseThrow();
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId()
        );

        log.debug("Обновлен пользователь с id: {}", user.getId());
        return getUserById(user.getId()).orElseThrow();
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        loadUserFriends(users);
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userMapper, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        loadUserFriends(users);
        return Optional.of(users.get(0));
    }

    @Override
    public void deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Удален пользователь с id: {}", id);
    }

    @Override
    public boolean containsUser(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status.name());
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<Long> getFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND status = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId, FriendshipStatus.CONFIRMED.name());
    }

    @Override
    public List<Long> getFriendIdsByStatus(Long userId, FriendshipStatus status) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND status = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId, status.name());
    }

    @Override
    public boolean existsFriend(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public void deleteAllFriendsByUserId(Long userId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(sql, userId, userId);
    }

    public List<Long> getUserIdsWithMostLikedFilmsMatches(Long userId) {
        String sql = """
                SELECT fl2.user_id
                FROM film_likes fl
                JOIN film_likes fl2 ON fl.film_id = fl2.film_id
                WHERE fl.user_id = ? AND fl2.user_id != ?
                GROUP BY fl2.user_id
                ORDER BY Count(fl2.user_id) DESC
                LIMIT 5;
                """;
        return jdbcTemplate.queryForList(sql, Long.class, userId, userId);
    }

    @Override
    public boolean isExistsLikedFilms(Long userId) {
        String sql = """
                SELECT COUNT(*)
                FROM film_likes
                WHERE user_id = ?;
                """;
        long numberRows = jdbcTemplate.queryForObject(sql, long.class, userId);
        log.info("numberRows = {}", numberRows);
        return numberRows != 0;
    }

    private void loadUserFriends(List<User> users) {
        if (users.isEmpty()) {
            return;
        }
        for (User user : users) {
            List<Long> friendIds = getFriendIds(user.getId());
            user.setFriends(new HashSet<>(friendIds));
        }
    }
}