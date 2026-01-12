package com.example.peoplecounter;

public class PeopleCounterApplication {

    public static void main(String[] args) throws Exception {


        MySQLDatabase db = new MySQLDatabase();


        WebServer.start(db);
        System.out.println(" People Counter + Webserver gestartet.");


        String monitoringRoom = "A201";   // ← HIER DEIN RAUM


        ArduinoListener listener = new ArduinoListener(db, monitoringRoom);
        listener.start();
    }
}