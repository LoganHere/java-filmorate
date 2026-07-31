package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        int directorId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(directorId);
        log.debug("Добавлен режиссёр с id: {}", directorId);
        return director;
    }

    @Override
    public List<Director> getAllDirectors() {
        String sql = """
                SELECT *
                FROM directors;
                """;
        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        try {
            String sql = """
                    SELECT *
                    FROM directors
                    WHERE id = ?;
                    """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    this::mapRowToDirector, id));
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр под ID " + id + " не найден.");
        }
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = """
                UPDATE directors SET name = ?
                WHERE id = ?;
                """;
        jdbcTemplate.update(sql, director.getName(), director.getId());
        log.debug("Обновлен режиссёр с id: {}", director.getId());
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        String sql = """
                DELETE
                FROM directors
                WHERE id = ?;
                """;
        jdbcTemplate.update(sql, id);
        log.debug("Удален режиссёр с id: {}", id);
    }

    @Override
    public boolean containsDirector(int id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("id"), rs.getString("name"));
    }
}