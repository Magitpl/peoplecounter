package com.example.peoplecounter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {

    public static void start(MySQLDatabase db) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // =========================
        // ROOMS
        // =========================
        server.createContext("/api/rooms", exchange -> {

            if (isOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "[]");
                return;
            }

            JSONArray arr = new JSONArray();
            for (Room r : db.getAllRooms()) {
                arr.put(new JSONObject()
                        .put("room_number", r.getRoomNumber())
                        .put("description", r.getDescription())
                );
            }

            sendResponse(exchange, 200, arr.toString());
        });

        // =========================
        // COUNT
        // =========================
        server.createContext("/api/count", exchange -> {

            if (isOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "{}");
                return;
            }

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 4) {
                sendResponse(exchange, 400, "{}");
                return;
            }

            int count = db.getCurrentCountForRoom(parts[3]);
            sendResponse(exchange, 200,
                    new JSONObject().put("count", count).toString());
        });

        // =========================
        // ENTRIES
        // =========================
        server.createContext("/api/entries", exchange -> {

            if (isOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "[]");
                return;
            }

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 4) {
                sendResponse(exchange, 400, "[]");
                return;
            }

            JSONArray arr = new JSONArray();
            for (Entry e : db.getEntriesForRoom(parts[3])) {
                arr.put(new JSONObject()
                        .put("name", e.name)
                        .put("room", e.room)
                        .put("entry_time", e.entry_time != null ? e.entry_time.toString() : null)
                        .put("exit_time", e.exit_time != null ? e.exit_time.toString() : null)
                );
            }

            sendResponse(exchange, 200, arr.toString());
        });

        // =========================
        // LINE
        // =========================
        server.createContext("/api/line", exchange -> {

            if (isOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "[]");
                return;
            }

            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 4) {
                sendResponse(exchange, 400, "[]");
                return;
            }

            sendResponse(exchange, 200,
                    db.getLineDataForRoom(parts[3]).toString());
        });

        // =========================
        // RFID
        // =========================
        server.createContext("/api/rfid", exchange -> {

            if (isOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{}");
                return;
            }

            JSONObject body = new JSONObject(
                    new String(exchange.getRequestBody().readAllBytes()));

            String cardId = body.getString("cardId");

            System.out.println("RFID gescannt: " + cardId);

            db.handleRFIDScan(cardId);

            sendResponse(exchange, 200, "{}");
        });

        server.start();
        System.out.println("✅ Backend läuft auf http://localhost:8080");
    }

    // =========================
    // HELPERS
    // =========================

    private static boolean isOptions(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(ex, 200, "");
            return true;
        }
        return false;
    }

    private static void sendResponse(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().set("Content-Type", "application/json");

        ex.sendResponseHeaders(status, body.getBytes().length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body.getBytes());
        }
    }
}