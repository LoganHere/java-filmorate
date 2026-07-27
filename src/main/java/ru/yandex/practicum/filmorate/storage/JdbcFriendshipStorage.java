package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;

@Slf4j
@Repository
public class JdbcFriendshipStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcFriendshipStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    public boolean exists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(sql, userId, userId);
    }
}