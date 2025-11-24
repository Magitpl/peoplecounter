package com.example.peoplecounter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccessController {
    private final MySQLDatabase db;
    private final Reader reader;
    private final String room; // z.B. "313"

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public AccessController(MySQLDatabase db, Reader reader, String room) {
        this.db = db;
        this.reader = reader;
        this.room = room;
    }

    // Neuer Schüler mit neuem Chip → registrieren & eintreten
    public void registerAndEnter(String chipIdInput, String name) {
        System.out.println("Schüler betritt Raum " + room + ": " + name);
        System.out.println("Schüler hält RFID-Chip an Reader ...");

        String chipId = reader.readChip(chipIdInput);
        int studentId = db.saveNewStudent(name, chipId);
        if (studentId == -1) {
            System.out.println("Fehler beim Speichern des Schülers.\n");
            return;
        }

        db.createEntryVisit(studentId, room);

        String now = LocalDateTime.now().format(fmt);
        showGreenSignal();
        System.out.println("Zutritt gewährt für " + name + " um " + now);
        System.out.println();

        db.printCurrentPeopleInRoom(room);
    }

    // Bekannter Schüler: Eintritt oder Austritt (Toggle)
    public void toggleEntryOrExit(String chipIdInput) {
        System.out.println("Schüler hält RFID-Chip an Reader ...");

        String chipId = reader.readChip(chipIdInput);
        Student s = db.findStudentByChip(chipId);

        if (s == null) {
            System.out.println("Unbekannter Chip! Zutritt verweigert.\n");
            return;
        }

        boolean inside = db.isStudentInsideRoom(s.getId(), room);
        String now = LocalDateTime.now().format(fmt);

        if (!inside) {
            System.out.println("-> " + s.getName() + " tritt in Raum " + room + " ein.");
            db.createEntryVisit(s.getId(), room);
            showGreenSignal();
            System.out.println("Eintrittszeit: " + now);
        } else {
            System.out.println("-> " + s.getName() + " verlässt Raum " + room + ".");
            db.closeOpenVisit(s.getId(), room);
            showGreenSignal();
            System.out.println("Austrittszeit: " + now);
        }

        System.out.println();
        db.printCurrentPeopleInRoom(room);
    }

    private void showGreenSignal() {
        System.out.println("[VISUELL] >>> GRÜNES LICHT AN <<<");
    }
}