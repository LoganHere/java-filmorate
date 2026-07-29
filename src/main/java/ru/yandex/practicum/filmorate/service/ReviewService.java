package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage,
                         FilmStorage filmStorage, EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventService = eventService;
    }

    public Review addReview(Review review) {
        validateReview(review);
        Review savedReview = reviewStorage.addReview(review);
        saveEvent(savedReview.getUserId(), savedReview.getReviewId(), Operation.ADD);
        return savedReview;
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("ID отзыва не может быть пустым");
        }
        getReviewById(review.getReviewId());
        Review updatedReview = reviewStorage.updateReview(review);
        saveEvent(updatedReview.getUserId(), updatedReview.getReviewId(), Operation.UPDATE);
        return updatedReview;
    }

    public void deleteReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        reviewStorage.deleteReview(review.getReviewId());
        saveEvent(review.getUserId(), review.getReviewId(), Operation.REMOVE);
    }

    public Review getReviewById(Long reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("Количество отзывов должно быть больше 0");
        }
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

    private void saveEvent(Long userId, Long entityId, Operation operation) {
        Event event = new Event();
        event.setUserId(userId);
        event.setEntityId(entityId);
        event.setEventType(EventType.REVIEW);
        event.setOperation(operation);
        eventService.addEvent(event);
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void validateReview(Review review) {
        validateUserExists(review.getUserId());
        if (!filmStorage.containsFilm(review.getFilmId())) {
            throw new NotFoundException("Фильм с id " + review.getFilmId() + " не найден");
        }
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        getReviewById(reviewId);
        validateUserExists(userId);
    }
}