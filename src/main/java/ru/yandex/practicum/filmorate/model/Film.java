package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class Film {
    private long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    @AssertTrue(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private boolean isValidReleaseDate() {
        return releaseDate == null || !releaseDate.isBefore(MIN_RELEASE_DATE);
    }

    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
}