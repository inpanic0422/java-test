package com.example.aipipe;

import java.util.List;
import java.util.Map;

public class Models {

    // ----------------
    // DB request model
    // ----------------
    public static class DbRequest {
        public String ifId;
        public String transId;
        public String isUrgent; // "Y" or "N"
        public List<StrInfo> strInfo; // list from DB
    }

    public static class StrInfo {
        public String strId;
        public String status;
        public String description;
    }

    // ----------------
    // Job file schema (Java -> Python)
    // ----------------
    public static class JobFile {
        public String job_id;
        public String if_id;
        public String trans_id;
        public String is_urgent;
        public String created_at;
        public List<JobStrInfo> str_info;
    }

    public static class JobStrInfo {
        public String str_id;
        public String status;
        public String description;
    }

    // ----------------
    // Result file schema (Python -> Java)
    // ----------------
    public static class ResultFile {
        public String job_id;
        public String if_id;
        public String trans_id;
        public String is_urgent;
        public String finished_at;
        public List<ResultItem> results;
    }

    public static class ResultItem {
        public String str_id;
        public boolean ok;
        public Map<String, Object> result;
        public String error;
    }
}
