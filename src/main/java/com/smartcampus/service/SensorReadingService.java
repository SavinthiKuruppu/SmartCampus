package com.smartcampus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryDataStore;

public class SensorReadingService {

    private final InMemoryDataStore store = InMemoryDataStore.getInstance();
    private final SensorService sensorService = new SensorService();
    private static final long ALLOWED_FUTURE_DRIFT_MILLIS = 60000L;

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        sensorService.getSensorById(sensorId);
        return new ArrayList<>(store.readingsForSensor(sensorId));
    }

    public synchronized SensorReading addReading(String sensorId, SensorReading request) {
        validateReading(request);
        Sensor sensor = sensorService.getSensorById(sensorId);
        if (sensor.isInMaintenance()) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is in MAINTENANCE status and cannot accept new readings.");
        }

        String id = request.getId() == null || request.getId().trim().isEmpty()
                ? "READING-" + UUID.randomUUID().toString()
                : request.getId().trim();
        long timestamp = normalizeTimestamp(request.getTimestamp());
        SensorReading reading = new SensorReading(id, timestamp, request.getValue());

        store.readingsForSensor(sensorId).add(reading);
        sensor.setCurrentValue(reading.getValue());
        return reading;
    }

    private void validateReading(SensorReading request) {
        if (request == null) {
            throw new BadRequestException("Sensor reading JSON body is required.");
        }
        if (Double.isNaN(request.getValue()) || Double.isInfinite(request.getValue())) {
            throw new BadRequestException("Sensor reading value must be a finite number.");
        }
        if (request.getTimestamp() < 0) {
            throw new BadRequestException("Sensor reading timestamp cannot be negative.");
        }
        if (request.getTimestamp() > System.currentTimeMillis() + ALLOWED_FUTURE_DRIFT_MILLIS) {
            throw new BadRequestException("Sensor reading timestamp cannot be in the future.");
        }
    }

    private long normalizeTimestamp(long timestamp) {
        return timestamp == 0 ? System.currentTimeMillis() : timestamp;
    }
}
