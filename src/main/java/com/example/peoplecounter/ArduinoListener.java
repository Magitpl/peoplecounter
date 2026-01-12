package com.example.peoplecounter;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.util.Scanner;

public class ArduinoListener extends Thread {

    private final MySQLDatabase db;
    private final String room;
    private SerialPort port;

    public ArduinoListener(MySQLDatabase db, String room) {
        this.db = db;
        this.room = room;
    }

    @Override
    public void run() {

        // ✅ PORT SUCHEN
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("❌ Kein Arduino gefunden!");
            return;
        }

        port = ports[0];
        port.setBaudRate(9600);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        port.openPort();

        System.out.println("✅ Arduino verbunden: " + port.getSystemPortName());

        InputStream in = port.getInputStream();
        Scanner scanner = new Scanner(in);

        while (true) {
            try {
                if (scanner.hasNextLine()) {

                    String chipId = scanner.nextLine().trim().toUpperCase();

                    // ✅ ALLE MÜLL-ZEILEN IGNORIEREN
                    if (chipId.isEmpty() ||
                            chipId.equals("READY") ||
                            chipId.equals("READREADY") ||
                            chipId.length() < 8) {
                        continue;
                    }

                    System.out.println("🔑 CHIP ERKANNT: [" + chipId + "]");

                    // ✅ EXISTIERT SCHÜLER?
                    if (!db.studentExistsByChip(chipId)) {
                        System.out.println("❌ Unbekannter Chip: " + chipId);
                        continue;
                    }

                    Student student = db.findStudentByChip(chipId);
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

                Thread.sleep(150);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}