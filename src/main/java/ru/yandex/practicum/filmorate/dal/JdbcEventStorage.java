package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository("jdbcEventStorage")
public class JdbcEventStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcEventStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Event addEvent(Event event) {
        long currentTime = System.currentTimeMillis();
        if (event.getTimestamp() == 0) {
            event.setTimestamp(currentTime);
        }

        String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, event.getTimestamp());
            ps.setLong(2, event.getUserId());
            ps.setString(3, event.getEventType().name());
            ps.setString(4, event.getOperation().name());
            ps.setLong(5, event.getEntityId());
            return ps;
        }, keyHolder);

        long eventId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        event.setEventId(eventId);

        log.debug("Добавлено событие с id: {}", eventId);
        return event;
    }

    @Override
    public List<Event> getEventsByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Event event = new Event();
            event.setTimestamp(rs.getLong("timestamp"));
            event.setEventId(rs.getLong("event_id"));
            event.setUserId(rs.getLong("user_id"));
            event.setEventType(EventType.valueOf(rs.getString("event_type")));
            event.setOperation(Operation.valueOf(rs.getString("operation")));
            event.setEntityId(rs.getLong("entity_id"));
            return event;
        }, userId);
    }
}
