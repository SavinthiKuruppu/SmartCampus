package com.smartcampus.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.InMemoryDataStore;

public class SensorService {

    private final InMemoryDataStore store = InMemoryDataStore.getInstance();
    private final RoomService roomService = new RoomService();

    public List<Sensor> getAllSensors(String type) {
        List<Sensor> sensors = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.trim().isEmpty()) {
            String filter = type.trim().toUpperCase(Locale.ROOT);
            sensors = sensors.stream()
                    .filter(sensor -> sensor.getType() != null
                            && sensor.getType().toUpperCase(Locale.ROOT).equals(filter))
                    .collect(Collectors.toList());
        }
        sensors.sort(Comparator.comparing(Sensor::getId));
        return sensors;
    }

    public Sensor getSensorById(String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return sensor;
    }

    public synchronized Sensor createSensor(Sensor request) {
        validateSensor(request);
        if (!roomService.exists(request.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Sensor cannot be created because roomId '" + request.getRoomId() + "' does not exist.");
        }

        String id = hasText(request.getId()) ? request.getId().trim() : "SENSOR-" + UUID.randomUUID().toString();
        if (store.getSensors().containsKey(id)) {
            throw new ResourceConflictException("Sensor already exists: " + id);
        }
        String status = hasText(request.getStatus()) ? request.getStatus().trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        Sensor sensor = new Sensor(id, clean(request.getType()).toUpperCase(Locale.ROOT), status,
                request.getCurrentValue(), request.getRoomId().trim());

        store.getSensors().put(sensor.getId(), sensor);
        roomService.addSensorToRoom(sensor.getRoomId(), sensor.getId());
        return sensor;
    }

    public boolean exists(String sensorId) {
        return sensorId != null && store.getSensors().containsKey(sensorId);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void validateSensor(Sensor request) {
        if (request == null) {
            throw new BadRequestException("Sensor JSON body is required.");
        }
        if (!hasText(request.getType())) {
            throw new BadRequestException("Sensor type is required.");
        }
        if (!hasText(request.getRoomId())) {
            throw new BadRequestException("Sensor roomId is required.");
        }
        if (hasText(request.getStatus())) {
            String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
            if (!"ACTIVE".equals(status) && !"MAINTENANCE".equals(status) && !"OFFLINE".equals(status)) {
                throw new BadRequestException("Sensor status must be ACTIVE, MAINTENANCE, or OFFLINE.");
            }
        }
    }
}
