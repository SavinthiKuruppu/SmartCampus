package com.smartcampus.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.InMemoryDataStore;

public class RoomService {

    private final InMemoryDataStore store = InMemoryDataStore.getInstance();

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        rooms.sort(Comparator.comparing(Room::getId));
        return rooms;
    }

    public Room getRoomById(String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }
        return room;
    }

    public synchronized Room createRoom(Room request) {
        validateRoom(request);
        String id = hasText(request.getId()) ? request.getId().trim() : "ROOM-" + UUID.randomUUID().toString();
        if (store.getRooms().containsKey(id)) {
            throw new ResourceConflictException("Room already exists: " + id);
        }
        Room room = new Room(id, clean(request.getName()), request.getCapacity(), request.getSensorIds());
        store.getRooms().put(room.getId(), room);
        return room;
    }

    public synchronized Room deleteRoom(String roomId) {
        Room room = getRoomById(roomId);
        if (room.hasSensors()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted because it still has sensors assigned.");
        }
        store.getRooms().remove(roomId);
        return room;
    }

    public boolean exists(String roomId) {
        return roomId != null && store.getRooms().containsKey(roomId);
    }

    public synchronized void addSensorToRoom(String roomId, String sensorId) {
        Room room = getRoomById(roomId);
        room.addSensorId(sensorId);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void validateRoom(Room request) {
        if (request == null) {
            throw new BadRequestException("Room JSON body is required.");
        }
        if (!hasText(request.getName())) {
            throw new BadRequestException("Room name is required.");
        }
        if (request.getCapacity() <= 0) {
            throw new BadRequestException("Room capacity must be greater than zero.");
        }
    }
}
