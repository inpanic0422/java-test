package com.example.aipipe;

public class Main {
    public static void main(String[] args) throws Exception {
        AppConfig cfg = new AppConfig();
        cfg.baseDir = System.getenv().getOrDefault("AI_PIPE_DIR", "C:\\ai_pipe");
        cfg.pythonBaseUrl = System.getenv().getOrDefault("PYTHON_URL", "http://127.0.0.1:8000");

        cfg.jdbcUrl = System.getenv("JDBC_URL");
        cfg.jdbcUser = System.getenv("JDBC_USER");
        cfg.jdbcPassword = System.getenv("JDBC_PASSWORD");

        String mode = "producer";
        for (String a : args) {
            if (a.startsWith("--mode=")) mode = a.substring("--mode=".length());
        }

        if ("producer".equalsIgnoreCase(mode)) {
            new Producer(cfg).run();
        } else if ("committer".equalsIgnoreCase(mode)) {
            new Committer(cfg).run();
        } else {
            System.out.println("Unknown mode. Use --mode=producer or --mode=committer");
        }
    }
}
