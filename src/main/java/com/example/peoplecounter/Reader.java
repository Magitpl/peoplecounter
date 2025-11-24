package com.example.peoplecounter;

public class Reader {
    public String readChip(String chipIdFromUser) {
        System.out.println("[Reader] Chip wird eingelesen ...");
        // In echt: hier würde der RFID-Reader ausgelesen
        return chipIdFromUser;
    }
}