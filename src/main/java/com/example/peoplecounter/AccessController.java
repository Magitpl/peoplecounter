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

        String chipId = reader.readChip();
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
    public void toggleEntryOrExit(String chipId) {

        Student student = db.findStudentByChip(chipId);
        if (student == null) {
            System.out.println("❌ Unbekannter Chip!");
            return;
        }

        boolean inside = db.isStudentInsideRoom(student.getId(), room);

        if (inside) {
            db.closeOpenVisit(student.getId(), room);
            System.out.println("⬅ " + student.getName() + " verlässt " + room);
        } else {
            db.createEntryVisit(student.getId(), room);
            System.out.println("➡ " + student.getName() + " betritt " + room);
        }

        db.printCurrentPeopleInRoom(room);
    }

    private void showGreenSignal() {
        System.out.println("[VISUELL] >>> GRÜNES LICHT AN <<<");
    }


}