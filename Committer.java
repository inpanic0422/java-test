package com.example.aipipe;

import com.example.aipipe.Models.ResultFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Committer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Committer.class);

    private final AppConfig cfg;
    private final ObjectMapper om = new ObjectMapper();

    private final Path doneUrgentDir;
    private final Path doneNormalDir;
    private final Path archiveResultsDir;

    public Committer(AppConfig cfg) throws Exception {
        this.cfg = cfg;
        this.doneUrgentDir = FileUtil.ensureDir(cfg.baseDir + "\\results\\done_urgent");
        this.doneNormalDir = FileUtil.ensureDir(cfg.baseDir + "\\results\\done_normal");
        this.archiveResultsDir = FileUtil.ensureDir(cfg.baseDir + "\\results\\archive_results");
    }

    @Override
    public void run() {
        while (true) {
            try {
                commitFromDir(doneUrgentDir);        // always
                if (isNormalWindow()) commitFromDir(doneNormalDir);
            } catch (Exception e) {
                log.error("commit loop failed", e);
            }
            try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException ignored) {}
        }
    }

    private boolean isNormalWindow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(cfg.normalCommitStart) && now.isBefore(cfg.normalCommitEnd);
    }

    private void commitFromDir(Path dir) throws Exception {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "result_*.json")) {
            for (Path p : stream) {
                files.add(p);
                if (files.size() >= cfg.commitBatchMaxFiles) break;
            }
        }
        if (files.isEmpty()) return;

        try (DbClient db = new DbClient(cfg.jdbcUrl, cfg.jdbcUser, cfg.jdbcPassword)) {
            int ok = 0;
            for (Path p : files) {
                try {
                    byte[] bytes = Files.readAllBytes(p);
                    ResultFile rf = om.readValue(bytes, ResultFile.class);

                    String idempotencyKey = rf.if_id + ":" + rf.trans_id;
                    db.upsertResult(idempotencyKey, rf.if_id, rf.trans_id, new String(bytes, "UTF-8"));

                    FileUtil.moveTo(p, archiveResultsDir);
                    ok++;
                } catch (Exception one) {
                    log.error("commit failed file=" + p, one);
                }
            }
            log.info("Committed {} files from {}", ok, dir.getFileName());
        }
    }
}
