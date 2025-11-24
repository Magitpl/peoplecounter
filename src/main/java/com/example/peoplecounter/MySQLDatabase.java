package com.example.peoplecounter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase {

    private final String url = "jdbc:mysql://localhost:3306/people_counter?serverTimezone=UTC";
    private final String user = "root";          // oder "root"
    private final String password = "Topal1015/";

    public MySQLDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL-Treiber nicht gefunden!");
        }
    }

    // ---------- Schüler / Chips ----------

    public boolean studentExistsByChip(String chipId) {
        String sql = "SELECT COUNT(*) FROM students WHERE chip_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chipId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int saveNewStudent(String name, String chipId) {
        String insertSql = "INSERT INTO students (name, chip_id) VALUES (?, ?)";
        String selectSql = "SELECT id FROM students WHERE chip_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, name);
                stmt.setString(2, chipId);
                stmt.executeUpdate();
            }
            System.out.println("[DB] Schüler '" + name + "' gespeichert.");

            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, chipId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Student findStudentByChip(String chipId) {
        String sql = "SELECT id, name, chip_id FROM students WHERE chip_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chipId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String chip = rs.getString("chip_id");
                return new Student(id, name, chip);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- Räume & Besuche ----------

    // Prüfen, ob Schüler in einem bestimmten Raum gerade drin ist
    public boolean isStudentInsideRoom(int studentId, String room) {
        String sql = "SELECT COUNT(*) FROM visits WHERE student_id = ? AND room = ? AND exit_time IS NULL";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, room);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eintritt in Raum speichern
    public void createEntryVisit(int studentId, String room) {
        String sql = "INSERT INTO visits (student_id, entry_time, room) VALUES (?, NOW(), ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, room);
            stmt.executeUpdate();
            System.out.println("[DB] Eintritt in Raum " + room + " gespeichert.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Austritt aus Raum speichern (letzten offenen Besuch schließen)
    public void closeOpenVisit(int studentId, String room) {
        String sql = """
                UPDATE visits
                SET exit_time = NOW()
                WHERE student_id = ? AND room = ? AND exit_time IS NULL
                ORDER BY entry_time DESC
                LIMIT 1
                """;
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, room);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("[DB] Austritt aus Raum " + room + " gespeichert.");
            } else {
                System.out.println("[DB] Kein offener Besuch für diesen Schüler in Raum " + room + " gefunden.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Aktuelle Zahl der Personen im Raum
    public int getCurrentCountForRoom(String room) {
        String sql = "SELECT COUNT(*) FROM visits WHERE room = ? AND exit_time IS NULL";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Liste aller Personen, die aktuell im Raum sind
    public void printCurrentPeopleInRoom(String room) {
        String sql = """
            SELECT s.name, v.entry_time
            FROM visits v
            JOIN students s ON v.student_id = s.id
            WHERE v.exit_time IS NULL AND v.room = ?
            ORDER BY v.entry_time
        """;
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room);
            ResultSet rs = stmt.executeQuery();

            System.out.println("=== Aktuell im Raum " + room + " ===");
            int count = 0;
            while (rs.next()) {
                String name = rs.getString("name");
                String time = rs.getString("entry_time");
                System.out.println("- " + name + " (seit " + time + ")");
                count++;
            }
            System.out.println("➡ Gesamt: " + count + " Person(en)\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Räume aus der Tabelle 'rooms' lesen ----------

    public List<Room> getAllRooms() {
        String sql = "SELECT id, room_number, description FROM rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String roomNumber = rs.getString("room_number");
                String desc = rs.getString("description");
                rooms.add(new Room(id, roomNumber, desc));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }
}