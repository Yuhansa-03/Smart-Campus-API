package com.mycompany.smartcampus_w2153601.store;

import com.mycompany.smartcampus_w2153601.exceptions.ApiConflictException;
import com.mycompany.smartcampus_w2153601.exceptions.ApiForbiddenException;
import com.mycompany.smartcampus_w2153601.exceptions.ApiUnprocessableEntityException;
import com.mycompany.smartcampus_w2153601.model.Room;
import com.mycompany.smartcampus_w2153601.model.Sensor;
import com.mycompany.smartcampus_w2153601.model.SensorReading;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class InMemoryStore {

    private static final InMemoryStore INSTANCE = new InMemoryStore();

    private final Map<Long, Room> rooms = new ConcurrentHashMap<Long, Room>();
    private final Map<Long, Sensor> sensors = new ConcurrentHashMap<Long, Sensor>();

    private final AtomicLong roomIds = new AtomicLong(1L);
    private final AtomicLong sensorIds = new AtomicLong(1L);
    private final AtomicLong readingIds = new AtomicLong(1L);

    private InMemoryStore() {
    }

    public static InMemoryStore getInstance() {
        return INSTANCE;
    }

    public Room createRoom(Room payload) {
        if (payload == null) {
            throw new ApiUnprocessableEntityException("Room payload is required.");
        }

        Room room = new Room();
        room.setId(roomIds.getAndIncrement());
        room.setName(requireText(payload.getName(), "name"));
        room.setBuilding(requireText(payload.getBuilding(), "building"));
        room.setFloor(payload.getFloor());

        rooms.put(room.getId(), room);
        return room;
    }

    public List<Room> getRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getId))
                .collect(Collectors.toList());
    }

    public Room getRoom(Long roomId) {
        return rooms.get(roomId);
    }

    public void deleteRoom(Long roomId) {
        Room existingRoom = rooms.get(roomId);

        if (existingRoom == null) {
            return;
        }

        boolean hasLinkedSensors = sensors.values().stream()
                .anyMatch(sensor -> roomId.equals(sensor.getRoomId()));

        if (hasLinkedSensors) {
            throw new ApiConflictException("Room " + roomId + " cannot be deleted because sensors are still assigned to it.");
        }

        rooms.remove(roomId);
    }

    public Sensor createSensor(Sensor payload) {
        if (payload == null) {
            throw new ApiUnprocessableEntityException("Sensor payload is required.");
        }

        if (payload.getRoomId() == null || !rooms.containsKey(payload.getRoomId())) {
            throw new ApiUnprocessableEntityException("roomId " + payload.getRoomId() + " does not exist.");
        }

        String type = requireText(payload.getType(), "type");

        if ("restricted".equalsIgnoreCase(type)) {
            throw new ApiForbiddenException("The selected sensor type is restricted and cannot be registered.");
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorIds.getAndIncrement());
        sensor.setRoomId(payload.getRoomId());
        sensor.setName(requireText(payload.getName(), "name"));
        sensor.setType(type);
        sensor.setUnit(requireText(payload.getUnit(), "unit"));
        sensor.setCurrentValue(payload.getCurrentValue());
        sensor.setReadings(new ArrayList<SensorReading>());

        sensors.put(sensor.getId(), sensor);
        return sensor;
    }

    public List<Sensor> getSensors(String type) {
        String normalizedType = type == null ? null : type.trim();

        return sensors.values().stream()
                .filter(sensor -> normalizedType == null
                || normalizedType.isEmpty()
                || sensor.getType().equalsIgnoreCase(normalizedType))
                .sorted(Comparator.comparing(Sensor::getId))
                .collect(Collectors.toList());
    }

    public Sensor getSensor(Long sensorId) {
        return sensors.get(sensorId);
    }

    public Sensor getSensorOrThrow(Long sensorId) {
        Sensor sensor = sensors.get(sensorId);

        if (sensor == null) {
            throw new ApiUnprocessableEntityException("sensorId " + sensorId + " does not exist.");
        }

        return sensor;
    }

    public List<SensorReading> getReadings(Long sensorId) {
        Sensor sensor = getSensorOrThrow(sensorId);

        synchronized (sensor) {
            return new ArrayList<SensorReading>(sensor.getReadings());
        }
    }

    public SensorReading addReading(Long sensorId, SensorReading payload) {
        if (payload == null) {
            throw new ApiUnprocessableEntityException("Reading payload is required.");
        }

        if (payload.getValue() == null) {
            throw new ApiUnprocessableEntityException("Field 'value' is required.");
        }

        Sensor sensor = getSensorOrThrow(sensorId);

        SensorReading reading = new SensorReading();
        reading.setId(readingIds.getAndIncrement());
        reading.setValue(payload.getValue());

        if (payload.getTimestamp() == null || payload.getTimestamp().trim().isEmpty()) {
            reading.setTimestamp(Instant.now().toString());
        } else {
            reading.setTimestamp(payload.getTimestamp().trim());
        }

        synchronized (sensor) {
            sensor.getReadings().add(reading);
            sensor.setCurrentValue(reading.getValue());
        }

        return reading;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiUnprocessableEntityException("Field '" + fieldName + "' is required.");
        }

        return value.trim();
    }
}