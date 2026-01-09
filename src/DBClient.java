package com.example.aipipe;

import com.example.aipipe.Models.DbRequest;

import java.sql.*;
import java.util.*;

public class DbClient implements AutoCloseable {
    private final Connection conn;

    public DbClient(String url, String user, String pass) throws SQLException {
        this.conn = DriverManager.getConnection(url, user, pass);
        this.conn.setAutoCommit(true);
    }

    // TODO: urgent 요청 조회 (is_urgent='Y')
    public List<DbRequest> fetchUrgent(int limit) throws SQLException {
        // 아래는 예시. 네 테이블/문법으로 반드시 수정.
        String sql = "SELECT id, object_index, idempotency_key, payload_json FROM request_table " +
                     "WHERE status='PENDING' AND is_urgent='Y' FETCH FIRST ? ROWS ONLY";
        List<DbRequest> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DbRequest r = new DbRequest();
                    r.rowId = rs.getString("id");
                    r.isUrgent = "Y";
                    r.objectIndex = rs.getInt("object_index");
                    r.idempotencyKey = rs.getString("idempotency_key");

                    // payload_json 안에 inner list가 있다고 가정하면,
                    // 여기서 파싱해서 innerPayloads로 넣어야 함.
                    // 지금은 TODO.
                    r.innerPayloads = new ArrayList<>();
                    out.add(r);
                }
            }
        }
        return out;
    }

    // TODO: normal 요청 조회
    public List<DbRequest> fetchNormal(int limit) throws SQLException {
        String sql = "SELECT id, object_index, idempotency_key, payload_json FROM request_table " +
                     "WHERE status='PENDING' AND is_urgent='N' FETCH FIRST ? ROWS ONLY";
        List<DbRequest> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DbRequest r = new DbRequest();
                    r.rowId = rs.getString("id");
                    r.isUrgent = "N";
                    r.objectIndex = rs.getInt("object_index");
                    r.idempotencyKey = rs.getString("idempotency_key");
                    r.innerPayloads = new ArrayList<>();
                    out.add(r);
                }
            }
        }
        return out;
    }

    // (선택) enqueue 마킹 (원하면 사용)
    public void markEnqueued(String rowId, int objectIndex) throws SQLException {
        String sql = "UPDATE request_table SET status='ENQUEUED' WHERE id=? AND object_index=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rowId);
            ps.setInt(2, objectIndex);
            ps.executeUpdate();
        }
    }

    // 결과 DB 반영 (중복 방지 유니크 키 권장)
    public void upsertResult(String idempotencyKey, String rowId, int objectIndex, String resultJson) throws SQLException {
        // 권장:
        // result_dedupe(idempotency_key PK)
        // result_table(...)
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO result_dedupe(idempotency_key) VALUES (?)")) {
                ps.setString(1, idempotencyKey);
                ps.executeUpdate();
            }
        } catch (SQLException dup) {
            // 이미 처리됨
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO result_table(row_id, object_index, idempotency_key, result_json) VALUES (?,?,?,?)")) {
            ps.setString(1, rowId);
            ps.setInt(2, objectIndex);
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
