package com.example.aipipe;

import java.time.LocalTime;

public class AppConfig {
    public String baseDir = "C:\\ai_pipe";
    public String pythonBaseUrl = "http://127.0.0.1:8000";

    public String jdbcUrl;
    public String jdbcUser;
    public String jdbcPassword;

    public int produceIntervalSeconds = 60;   // 주기
    public int fetchBatchSizeUrgent = 50;
    public int fetchBatchSizeNormal = 20;

    // normal 결과 DB 반영 시간대
    public LocalTime normalCommitStart = LocalTime.of(1, 0);
    public LocalTime normalCommitEnd   = LocalTime.of(5, 0);

    // committer batch
    public int commitBatchMaxFiles = 500;
}
