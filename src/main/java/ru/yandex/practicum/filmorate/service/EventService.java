package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

@Slf4j
@Service
public class EventService {

    private final EventStorage eventStorage;

    @Autowired
    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public List<Event> getFeed(Long userId) {
        log.debug("Запрос ленты событий для пользователя {}", userId);
        return eventStorage.getEventsByUserId(userId);
    }

    public void saveEvent(Long userId, Long entityId, EventType eventType, Operation operation) {
        Event event = new Event();
        event.setUserId(userId);
        event.setEntityId(entityId);
        event.setEventType(eventType);
        event.setOperation(operation);
        eventStorage.addEvent(event);
        log.debug("Событие сохранено: userId={}, entityId={}, type={}, operation={}",
                userId, entityId, eventType, operation);
    }
}