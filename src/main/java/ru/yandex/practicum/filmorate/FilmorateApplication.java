package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {
    public static void main(String[] args) {
        //Кинопоиск бойся, грядут конкуренты
        SpringApplication.run(FilmorateApplication.class, args);
    }
}
