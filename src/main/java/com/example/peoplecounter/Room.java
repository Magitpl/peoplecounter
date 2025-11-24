package com.example.peoplecounter;


public class Room {
    private final int id;
    private final String roomNumber;
    private final String description;

    public Room(int id, String roomNumber, String description) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getDescription() {
        return description;
    }
}