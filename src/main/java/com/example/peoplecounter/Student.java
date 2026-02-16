package com.example.peoplecounter;
public class Student {
    private final int id;
    private final String name;
    private final String chipId;

    public Student(int id, String name, String chipId) {
        this.id = id;
        this.name = name;
        this.chipId = chipId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getChipId() {
        return chipId;
    }



}