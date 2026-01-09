package com.example.aipipe;

import com.example.aipipe.Models.DbRequest;
import com.example.aipipe.Models.JobFile;
import com.example.aipipe.Models.JobStrInfo;
import com.example.aipipe.Models.StrInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Producer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    private final AppConfig cfg;
    private final ObjectMapper om = new ObjectMapper();

    private final Path readyUrgentDir;
    private final Path readyNormalDir;

    public Producer(AppConfig cfg) throws Exception {
        this.cfg = cfg;
        this.readyUrgentDir = FileUtil.ensureDir(cfg.baseDir + "\\jobs\\ready_urgent");
        this.readyNormalDir = FileUtil.ensureDir(cfg.baseDir + "\\jobs\\ready_normal");
        FileUtil.ensureDir(cfg.baseDir + "\\jobs\\working");
        FileUtil.ensureDir(cfg.baseDir + "\\jobs\\archive_jobs");
        FileUtil.ensureDir(cfg.baseDir + "\\results\\done_urgent");
        FileUtil.ensureDir(cfg.baseDir + "\\results\\done_normal");
    }

    @Override
    public void run() {
        while (true) {
            try {
                produceOnce();
            } catch (Exception e) {
                log.error("produceOnce failed", e);
            }
            try { TimeUnit.SECONDS.sleep(cfg.produceIntervalSeconds); } catch (InterruptedException ignored) {}
        }
    }

    private void produceOnce() throws Exception {
        try (DbClient db = new DbClient(cfg.jdbcUrl, cfg.jdbcUser, cfg.jdbcPassword)) {
            List<DbRequest> urgents = db.fetchUrgent(cfg.fetchBatchSizeUrgent);
            for (DbRequest r : urgents) enqueueOne(r);

            List<DbRequest> normals = db.fetchNormal(cfg.fetchBatchSizeNormal);
            for (DbRequest r : normals) enqueueOne(r);

            log.info("Produced urgent={}, normal={}", urgents.size(), normals.size());
        }
    }

    private void enqueueOne(DbRequest r) throws Exception {
        String jobId = UUID.randomUUID().toString();
        boolean urgent = "Y".equalsIgnoreCase(r.isUrgent);

        JobFile job = new JobFile();
        job.job_id = jobId;
        job.if_id = r.ifId;
        job.trans_id = r.transId;
        job.is_urgent = urgent ? "Y" : "N";
        job.created_at = OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString();

        List<JobStrInfo> list = new ArrayList<>();
        if (r.strInfo != null) {
            for (StrInfo si : r.strInfo) {
                JobStrInfo ji = new JobStrInfo();
                ji.str_id = si.strId;
                ji.status = si.status;
                ji.description = si.description;
                list.add(ji);
            }
        }
        job.str_info = list;

        Path targetDir = urgent ? readyUrgentDir : readyNormalDir;
        String fileName = String.format("job_%s_%s_%s.json", safe(r.ifId), safe(r.transId), jobId);
        Path jobPath = targetDir.resolve(fileName);

        byte[] bytes = om.writeValueAsBytes(job);
        FileUtil.atomicWrite(jobPath, bytes);

        String idempotencyKey = r.ifId + ":" + r.transId;

        // Call Python enqueue with path only
        String body = String.format(
                "{\"job_path\":\"%s\",\"idempotency_key\":\"%s\",\"is_urgent\":\"%s\",\"size_bytes\":%d}",
                escapeJson(jobPath.toString()),
                escapeJson(idempotencyKey),
                urgent ? "Y" : "N",
                bytes.length
        );

        int code = HttpClientSimple.postJson(cfg.pythonBaseUrl + "/enqueue", body);
        log.info("Enqueue {} if_id={} trans_id={} file={} http={}", urgent ? "URGENT" : "NORMAL", r.ifId, r.transId, jobPath.getFileName(), code);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String safe(String s) {
        if (s == null) return "null";
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
