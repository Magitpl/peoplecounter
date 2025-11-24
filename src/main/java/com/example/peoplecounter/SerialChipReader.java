package com.example.peoplecounter;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;

public class SerialChipReader {
    private final SerialPort port;
    private final Scanner scanner;

    public SerialChipReader(String portName) {
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(9600);

        if (!port.openPort()) {
            throw new RuntimeException("Konnte Port " + portName + " nicht öffnen");
        }

        scanner = new Scanner(port.getInputStream());
        System.out.println("Serieller Port geöffnet: " + portName);
    }

    /**
     * Liest die nächste Zeile vom Arduino (eine UID) oder gibt null zurück,
     * wenn noch nichts angekommen ist.
     */
    public String readChipId() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return null;
        }
        System.out.println("[Serial] Empfangen: " + line);
        return line;
    }

    public void close() {
        try {
            scanner.close();
        } catch (Exception ignored) {}
        try {
            port.closePort();
        } catch (Exception ignored) {}
    }


    public static void listPorts() {
        for (SerialPort p : SerialPort.getCommPorts()) {
            System.out.println("Port: " + p.getSystemPortName());
        }
    }
}