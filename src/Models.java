package com.example.aipipe;

import java.util.List;
import java.util.Map;

public class Models {

    public static class Job {
        public String job_id;
        public String idempotency_key;
        public String row_id;
        public int object_index;
        public String is_urgent; // "Y" or "N"
        public String created_at;
        public List<InnerItem> inner_items;
    }

    public static class InnerItem {
        public int inner_index;
        public Map<String, Object> payload;
    }

    public static class ResultFile {
        public String job_id;
        public String idempotency_key;
        public String row_id;
        public int object_index;
        public String is_urgent;
        public String created_at;
        public String finished_at;
        public List<ResultItem> items;
    }

    public static class ResultItem {
        public int inner_index;
        public boolean ok;
        public Map<String, Object> response;
        public String error;
    }

    // DB에서 읽어오는 “요청” 모델(예시)
    public static class DbRequest {
        public String rowId;
        public String isUrgent;     // "Y" or "N"
        public int objectIndex;     // Object 단위 분리한 인덱스
        public String idempotencyKey;
        public List<Map<String, Object>> innerPayloads; // inner_items payload 목록
    }
}
