package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Запрос на добавление отзыва: userId={}, filmId={}", review.getUserId(), review.getFilmId());
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Запрос на обновление отзыва с id: {}", review.getReviewId());
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("id") Long reviewId) {
        log.info("Запрос на удаление отзыва с id: {}", reviewId);
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") Long reviewId) {
        log.debug("Запрос на получение отзыва с id: {}", reviewId);
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        log.debug("Запрос на получение отзывов: filmId={}, count={}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} удалил лайк с отзыва {}", userId, reviewId);
        reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        log.info("Пользователь {} удалил дизлайк с отзыва {}", userId, reviewId);
        reviewService.removeDislike(reviewId, userId);
    }
}