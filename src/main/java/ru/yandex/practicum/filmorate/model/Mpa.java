package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Mpa {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    Mpa(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    public static Mpa fromJson(@JsonProperty("id") int id) {
        for (Mpa mpa : values()) {
            if (mpa.id == id) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("Unknown MPA id: " + id);
    }

    public static Mpa fromId(int id) {
        for (Mpa mpa : values()) {
            if (mpa.id == id) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("Unknown MPA id: " + id);
    }
}