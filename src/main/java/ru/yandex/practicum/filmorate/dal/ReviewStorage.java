package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long reviewId);

    Optional<Review> getReviewById(Long reviewId);

    List<Review> getReviewsByFilmId(Long filmId, int count);

    List<Review> getAllReviews(int count);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);

    int getUsefulCount(Long reviewId);
}