package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage, FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review addReview(Review review) {
        validateReview(review);
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("ID отзыва не может быть пустым");
        }
        if (!reviewStorage.getReviewById(review.getReviewId()).isPresent()) {
            throw new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден");
        }
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long reviewId) {
        if (!reviewStorage.getReviewById(reviewId).isPresent()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(Long reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (filmId != null) {
            return reviewStorage.getReviewsByFilmId(filmId, count);
        }
        return reviewStorage.getAllReviews(count);
    }

    public void addLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (!userStorage.containsUser(review.getUserId())) {
            throw new NotFoundException("Пользователь с id " + review.getUserId() + " не найден");
        }
        if (!filmStorage.containsFilm(review.getFilmId())) {
            throw new NotFoundException("Фильм с id " + review.getFilmId() + " не найден");
        }
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        if (!reviewStorage.getReviewById(reviewId).isPresent()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}