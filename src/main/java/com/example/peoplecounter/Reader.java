package com.example.peoplecounter;

import java.util.Scanner;

public class Reader {

    private final Scanner scanner = new Scanner(System.in);

    // ✅ Liest eine Chip-ID aus der Konsole (KEINE Parameter!)
    public String readChip() {
        System.out.print("Chip-ID: ");
        return scanner.nextLine().trim();
    }
}