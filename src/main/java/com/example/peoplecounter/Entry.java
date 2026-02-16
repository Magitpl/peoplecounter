package com.example.peoplecounter;

import java.sql.Timestamp;

public class Entry {

    public int id;
    public String name;
    public String room;
    public Timestamp entry_time;
    public Timestamp exit_time;

    public Entry(int id, String name, String room, Timestamp entry_time, Timestamp exit_time) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.entry_time = entry_time;
        this.exit_time = exit_time;
    }
}