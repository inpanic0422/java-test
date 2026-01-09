package com.example.aipipe;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class FileUtil {

    public static Path ensureDir(String dir) throws IOException {
        Path p = Paths.get(dir);
        Files.createDirectories(p);
        return p;
    }

    public static void atomicWrite(Path target, byte[] bytes) throws IOException {
        Path dir = target.getParent();
        Files.createDirectories(dir);
        Path tmp = dir.resolve(".tmp_" + UUID.randomUUID() + ".json");
        Files.write(tmp, bytes, StandardOpenOption.CREATE_NEW);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void moveTo(Path src, Path dstDir) throws IOException {
        Files.createDirectories(dstDir);
        Path dst = dstDir.resolve(src.getFileName().toString());
        Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
    }
}
