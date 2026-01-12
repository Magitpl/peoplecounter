package com.example.peoplecounter;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {

    public static void start(MySQLDatabase db) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        server.createContext("/api/login", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject req = new JSONObject(body);

                String email = req.getString("email");
                String password = req.getString("password");

                JSONObject res = new JSONObject();

                if (email.equals("admin@test.com") && password.equals("1234")) {
                    res.put("token", "mysecrettoken123");
                    sendResponse(exchange, 200, res.toString());
                } else {
                    res.put("error", "Invalid credentials");
                    sendResponse(exchange, 401, res.toString());
                }

            } else {
                sendResponse(exchange, 405, "{\"error\":\"Only POST allowed\"}");
            }
        });


        server.createContext("/api/rooms", exchange -> {
            JSONArray arr = new JSONArray();

            db.getAllRooms().forEach(r -> {
                arr.put(new JSONObject()
                        .put("room_number", r.getRoomNumber())
                        .put("description", r.getDescription()));
            });

            sendResponse(exchange, 200, arr.toString());
        });


        server.createContext("/api/room", exchange -> {

            String path = exchange.getRequestURI().getPath(); // /api/room/A201
            String[] parts = path.split("/");

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"room missing\"}");
                return;
            }

            String room = parts[3].toUpperCase(); // ✅ GROSSSCHREIBUNG FIX
            JSONArray arr = db.getCurrentPeopleAsJson(room);

            sendResponse(exchange, 200, arr.toString());
        });


        server.createContext("/api/devices", exchange -> {
            JSONArray arr = db.getDevicesAsJson();
            sendResponse(exchange, 200, arr.toString());
        });


        server.createContext("/api/permissions", exchange -> {
            JSONArray arr = db.getPermissionsAsJson();
            sendResponse(exchange, 200, arr.toString());
        });


        server.createContext("/api/count", exchange -> {

            String path = exchange.getRequestURI().getPath(); // /api/count/A201
            String[] parts = path.split("/");

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"room missing\"}");
                return;
            }

            String room = parts[3].toUpperCase(); // ✅ GROSSSCHREIBUNG FIX
            int count = db.getCurrentCountForRoom(room);

            JSONObject res = new JSONObject();
            res.put("room", room);
            res.put("count", count);

            sendResponse(exchange, 200, res.toString());
        });


        server.createContext("/api/visits", exchange -> {
            JSONArray arr = db.getAllVisitsAsJson();
            sendResponse(exchange, 200, arr.toString());
        });


        server.createContext("/api/line", exchange -> {

            String path = exchange.getRequestURI().getPath(); // /api/line/A201
            String[] parts = path.split("/");

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"room missing\"}");
                return;
            }

            String room = parts[3].toUpperCase(); // ✅ GROSSSCHREIBUNG FIX
            JSONArray arr = db.getLineDataForRoom(room);

            sendResponse(exchange, 200, arr.toString());
        });


        server.start();
        System.out.println("➡ Webserver läuft unter http://localhost:8080/");
    }



    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(status, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}