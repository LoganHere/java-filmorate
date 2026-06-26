package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    private Set<Long> likes = new HashSet<>();

    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @AssertTrue(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private boolean isValidReleaseDate() {
        return releaseDate == null || !releaseDate.isBefore(MIN_RELEASE_DATE);
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }

    public boolean isLikedBy(Long userId) {
        return likes.contains(userId);
    }

    public int getLikesCount() {
        return likes.size();
    }
}