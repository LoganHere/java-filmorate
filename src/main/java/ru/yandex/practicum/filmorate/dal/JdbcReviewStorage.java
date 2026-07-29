package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class JdbcReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcReviewStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        long reviewId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(reviewId);
        review.setUseful(0);

        log.debug("Добавлен отзыв с id: {}", reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        log.debug("Обновлен отзыв с id: {}", review.getReviewId());
        return getReviewById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void deleteReview(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId);
        log.debug("Удален отзыв с id: {}", reviewId);
    }

    @Override
    public Optional<Review> getReviewById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        addRating(reviewId, userId, true);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        addRating(reviewId, userId, false);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    @Override
    public int getUsefulCount(Long reviewId) {
        String sql = "SELECT useful FROM reviews WHERE review_id = ?";
        Integer useful = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return useful != null ? useful : 0;
    }

    private void addRating(Long reviewId, Long userId, boolean isLike) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE review_likes SET is_like = ? WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, isLike, reviewId, userId);
        } else {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, reviewId, userId, isLike);
        }

        updateUsefulCount(reviewId);
    }

    private void updateUsefulCount(Long reviewId) {
        String sql = "UPDATE reviews SET useful = " +
                "(SELECT COALESCE(SUM(CASE WHEN is_like = true THEN 1 ELSE -1 END), 0) " +
                "FROM review_likes WHERE review_id = ?) " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    private Review mapRowToReview(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getInt("useful")
        );
    }
}