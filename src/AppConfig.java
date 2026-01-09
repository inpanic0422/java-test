package com.example.aipipe;

import java.time.LocalTime;

public class AppConfig {
    public String baseDir = System.getenv().getOrDefault("AI_PIPE_DIR", "C:\\ai_pipe");
    public String pythonBaseUrl = System.getenv().getOrDefault("PYTHON_URL", "http://127.0.0.1:8000");

    public String jdbcUrl = System.getenv("JDBC_URL");
    public String jdbcUser = System.getenv("JDBC_USER");
    public String jdbcPassword = System.getenv("JDBC_PASSWORD");

    public int produceIntervalSeconds = 60;

    public int fetchBatchSizeUrgent = 50;
    public int fetchBatchSizeNormal = 20;

    // normal result commit time window (local time)
    public LocalTime normalCommitStart = LocalTime.of(1, 0);
    public LocalTime normalCommitEnd   = LocalTime.of(5, 0);

    public int commitBatchMaxFiles = 500;
}
