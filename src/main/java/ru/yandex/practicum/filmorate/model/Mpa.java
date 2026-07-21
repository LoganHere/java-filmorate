package ru.yandex.practicum.filmorate.model;

public enum Mpa {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String code;

    Mpa(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}