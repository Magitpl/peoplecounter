package com.example.peoplecounter;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {

    public static void start(MySQLDatabase db) throws IOException {
        // Server auf Port 8080 starten
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Endpoint: /api/count/<room>
        server.createContext("/api/count", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"room missing\"}");
                return;
            }

            String room = parts[3];

            int count = db.getCurrentCountForRoom(room);

            String json = "{ \"room\": \"" + room + "\", \"count\": " + count + " }";
            sendResponse(exchange, 200, json);
        });

        System.out.println("➡ Webserver läuft unter http://localhost:8080/");
        server.start();
    }

    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}