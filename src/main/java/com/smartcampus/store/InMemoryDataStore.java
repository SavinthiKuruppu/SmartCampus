package com.smartcampus.store;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

public final class InMemoryDataStore {

    private static final InMemoryDataStore INSTANCE = new InMemoryDataStore();

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<SensorReading>> readings = new ConcurrentHashMap<>();

    private InMemoryDataStore() {
        seedData();
    }

    public static InMemoryDataStore getInstance() {
        return INSTANCE;
    }

    public ConcurrentMap<String, Room> getRooms() {
        return rooms;
    }

    public ConcurrentMap<String, Sensor> getSensors() {
        return sensors;
    }

    public List<SensorReading> readingsForSensor(String sensorId) {
        return readings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>());
    }

    private void seedData() {
        Room lectureHall = new Room("R-101", "Lecture Hall 101", 120);
        Room lab = new Room("LAB-2", "Computer Lab 2", 35);
        rooms.put(lectureHall.getId(), lectureHall);
        rooms.put(lab.getId(), lab);

        Sensor co2Sensor = new Sensor("S-CO2-1", "CO2", "ACTIVE", 415.0, lectureHall.getId());
        Sensor tempSensor = new Sensor("S-TEMP-1", "TEMPERATURE", "MAINTENANCE", 22.5, lectureHall.getId());
        sensors.put(co2Sensor.getId(), co2Sensor);
        sensors.put(tempSensor.getId(), tempSensor);

        lectureHall.addSensorId(co2Sensor.getId());
        lectureHall.addSensorId(tempSensor.getId());

        readingsForSensor(co2Sensor.getId()).add(new SensorReading("READING-SEED-1", System.currentTimeMillis(), 415.0));
    }
}
