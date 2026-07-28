package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private long timestamp;
    private long eventId;
    private long userId;
    private long entityId;
    private EventType eventType;
    private Operation operation;
}
