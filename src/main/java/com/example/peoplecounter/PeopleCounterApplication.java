package com.example.peoplecounter;

import java.util.List;
import java.util.Scanner;

public class PeopleCounterApplication {

    public static void main(String[] args) throws Exception {

        // Datenbank erstellen
        MySQLDatabase db = new MySQLDatabase();

        // ⭐ WEB-SERVER STARTEN (für deine WebStorm-Seite)
        WebServer.start(db);

        // Nur zur Info
        System.out.println("People Counter + Webserver gestartet.");

        // ------------------------------
        // Simulation ohne Arduino
        // ------------------------------

        Scanner scanner = new Scanner(System.in);
        Reader reader = new Reader();

        System.out.println("---- Räume aus Datenbank ----");
        List<Room> rooms = db.getAllRooms();
        for (Room r : rooms) {
            System.out.println("- " + r.getRoomNumber() + "  (" + r.getDescription() + ")");
        }

        System.out.print("Welchen Raum möchtest du überwachen? ");
        String roomNumber = scanner.nextLine().trim();

        AccessController controller = new AccessController(db, reader, roomNumber);

        System.out.println("SIMULATIONSMODUS aktiviert.");
        System.out.println("Chip-IDs eingeben, oder 'exit' zum Beenden.");

        while (true) {
            System.out.print("Chip-ID: ");
            String chipId = scanner.nextLine();

            if (chipId.equalsIgnoreCase("exit")) {
                System.out.println("Programm beendet.");
                break;
            }

            if (!db.studentExistsByChip(chipId)) {
                System.out.print("Name des Schülers: ");
                String name = scanner.nextLine();
                controller.registerAndEnter(chipId, name);
            } else {
                controller.toggleEntryOrExit(chipId);
            }
        }
    }
}