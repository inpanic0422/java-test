package com.example.aipipe;

public class Main {
    public static void main(String[] args) throws Exception {
        AppConfig cfg = new AppConfig();

        String mode = "producer";
        for (String a : args) {
            if (a.startsWith("--mode=")) {
                mode = a.substring("--mode=".length());
            }
        }

        if (cfg.jdbcUrl == null || cfg.jdbcUser == null || cfg.jdbcPassword == null) {
            System.out.println("Missing JDBC env vars: JDBC_URL/JDBC_USER/JDBC_PASSWORD");
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
