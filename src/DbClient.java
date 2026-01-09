package com.example.aipipe;

import com.example.aipipe.Models.DbRequest;
import com.example.aipipe.Models.StrInfo;

import java.sql.*;
import java.util.*;

/**
 * DB access layer (Java 8).
 *
 * IMPORTANT:
 * - Replace SQL and parsing logic to match your DB schema.
 * - The payload (str_info list) may be stored as JSON in a column; parse it to List<StrInfo>.
 */
public class DbClient implements AutoCloseable {
    private final Connection conn;

    public DbClient(String url, String user, String pass) throws SQLException {
        this.conn = DriverManager.getConnection(url, user, pass);
        this.conn.setAutoCommit(true);
    }

    public List<DbRequest> fetchUrgent(int limit) throws SQLException {
        // TODO: Replace with your actual SQL
        String sql =
            "SELECT if_id, trans_id, is_urgent, str_info_json " +
            "FROM request_table " +
            "WHERE status='PENDING' AND is_urgent='Y' " +
            "FETCH FIRST ? ROWS ONLY";

        return fetch(sql, limit);
    }

    public List<DbRequest> fetchNormal(int limit) throws SQLException {
        // TODO: Replace with your actual SQL
        String sql =
            "SELECT if_id, trans_id, is_urgent, str_info_json " +
            "FROM request_table " +
            "WHERE status='PENDING' AND is_urgent='N' " +
            "FETCH FIRST ? ROWS ONLY";

        return fetch(sql, limit);
    }

    private List<DbRequest> fetch(String sql, int limit) throws SQLException {
        List<DbRequest> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DbRequest r = new DbRequest();
                    r.ifId = rs.getString("if_id");
                    r.transId = rs.getString("trans_id");
                    r.isUrgent = rs.getString("is_urgent");
                    // TODO: parse str_info_json -> List<StrInfo>
                    // Example: r.strInfo = objectMapper.readValue(..., new TypeReference<List<StrInfo>>(){});
                    r.strInfo = new ArrayList<StrInfo>();
                    out.add(r);
                }
            }
        }
        return out;
    }

    public void upsertResult(String idempotencyKey, String ifId, String transId, String resultJson) throws SQLException {
        // Recommended approach:
        // 1) Insert into dedupe table with UNIQUE constraint on idempotency_key
        // 2) Insert result row only if dedupe insert succeeded
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO result_dedupe(idempotency_key) VALUES (?)")) {
                ps.setString(1, idempotencyKey);
                ps.executeUpdate();
            }
        } catch (SQLException dup) {
            // Already committed
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO result_table(if_id, trans_id, idempotency_key, result_json) VALUES (?,?,?,?)")) {
            ps.setString(1, ifId);
            ps.setString(2, transId);
            ps.setString(3, idempotencyKey);
            ps.setString(4, resultJson);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }
}
