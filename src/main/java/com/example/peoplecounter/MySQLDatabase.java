package com.example.peoplecounter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase {

    String url = "jdbc:mysql://localhost:3306/peoplecounter";
    String user = "peopleuser";
    String password = "1234";

    public MySQLDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL-Treiber nicht gefunden!");
        }
    }



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
                return new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("chip_id")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



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

    public void createEntryVisit(int studentId, String room) {
        String sql = "INSERT INTO visits (student_id, entry_time, room) VALUES (?, NOW(), ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, room);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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



    public JSONArray getCurrentPeopleAsJson(String room) {
        JSONArray arr = new JSONArray();

        String sql = """
            SELECT s.name, v.entry_time
            FROM visits v
            JOIN students s ON v.student_id = s.id
            WHERE v.room = ? AND v.exit_time IS NULL
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, room);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("name", rs.getString("name"))
                        .put("entry_time", rs.getString("entry_time")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }

    // ==============================
    // ✅ ALLE VISITS (JSON)
    // ==============================

    public JSONArray getAllVisitsAsJson() {
        JSONArray arr = new JSONArray();

        String sql = "SELECT * FROM visits";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("student_id", rs.getInt("student_id"))
                        .put("room", rs.getString("room"))
                        .put("entry_time", rs.getString("entry_time"))
                        .put("exit_time", rs.getString("exit_time")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }

    // ==============================
    // ✅ RÄUME
    // ==============================

    public List<Room> getAllRooms() {

        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, room_number, description FROM rooms ORDER BY room_number";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }


    public JSONArray getDevicesAsJson() {
        JSONArray arr = new JSONArray();

        String sql = "SELECT * FROM devices";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("name", rs.getString("name"))
                        .put("location", rs.getString("location"))
                        .put("owner", rs.getString("owner"))
                        .put("date", rs.getString("date")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }



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



    public JSONArray getPermissionsAsJson() {
        JSONArray arr = new JSONArray();

        String sql = "SELECT * FROM permissions";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("role", rs.getString("role"))
                        .put("user", rs.getString("user"))
                        .put("date", rs.getString("date"))
                        .put("owner", rs.getString("owner")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }

    public JSONArray getLineChartData(String room) {

        JSONArray arr = new JSONArray();

        String sql = """
        SELECT DATE_FORMAT(entry_time, '%H:%i') AS time, COUNT(*) AS count
        FROM visits
        WHERE room = ?
        GROUP BY time
        ORDER BY time
    """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("time", rs.getString("time"))
                        .put("count", rs.getInt("count")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arr;
    }
    // ✅ LINE-CHART-DATEN (Besucher je Minute)
    public JSONArray getLineDataForRoom(String room) {
        JSONArray arr = new JSONArray();

        String sql = """
        SELECT DATE_FORMAT(entry_time, '%H:%i') AS time_label,
               COUNT(*) AS countvalue
        FROM visits
        WHERE room = ?
        GROUP BY time_label
        ORDER BY time_label
    """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                arr.put(new JSONObject()
                        .put("time", rs.getString("time_label"))
                        .put("value", rs.getInt("countvalue")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return arr;
    }
}