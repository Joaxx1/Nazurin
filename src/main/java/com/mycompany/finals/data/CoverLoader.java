package com.mycompany.finals.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;

/**
 * Downloads cover pictures in the background and keeps them in memory and in a folder
 * on this computer, so each picture is downloaded only once.
 */
public class CoverLoader {

    private static final int MAX_WIDTH = 640;
    private static final int MEMORY_LIMIT = 80;

    private final Path cacheDir;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ExecutorService workers = Executors.newFixedThreadPool(4, task -> {
        Thread thread = new Thread(task, "cover-loader");
        thread.setDaemon(true);
        return thread;
    });
    /** Most recently used pictures; the oldest is dropped once the limit is reached. */
    private final Map<String, BufferedImage> memory = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > MEMORY_LIMIT;
                }
            });
    /** Downloads in progress, so two cards showing the same game share one download. */
    private final Map<String, CompletableFuture<BufferedImage>> pending = new ConcurrentHashMap<>();

    public CoverLoader(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    /** The picture if it is already in memory, otherwise null. */
    public BufferedImage cached(String url) {
        return url == null || url.isEmpty() ? null : memory.get(url);
    }

    /** Loads a picture. The result is null when there is no picture or it could not be loaded. */
    public CompletableFuture<BufferedImage> load(String url) {
        if (url == null || url.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        BufferedImage inMemory = memory.get(url);
        if (inMemory != null) {
            return CompletableFuture.completedFuture(inMemory);
        }
        CompletableFuture<BufferedImage> future = new CompletableFuture<>();
        CompletableFuture<BufferedImage> alreadyLoading = pending.putIfAbsent(url, future);
        if (alreadyLoading != null) {
            return alreadyLoading;
        }
        workers.execute(() -> {
            BufferedImage image = fetch(url);
            if (image != null) {
                memory.put(url, image);
            }
            pending.remove(url);
            future.complete(image);
        });
        return future;
    }

    private BufferedImage fetch(String url) {
        Path file = cacheDir.resolve(fileNameFor(url));
        try {
            boolean onDisk = Files.isRegularFile(file);
            byte[] bytes = onDisk ? Files.readAllBytes(file) : download(url);
            if (bytes == null) {
                return null;
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null; // not a picture format Java can read
            }
            if (!onDisk) {
                saveToDisk(file, bytes);
            }
            return shrink(image);
        } catch (IOException | RuntimeException e) {
            // A missing cover is not worth an error message: the screens show a placeholder instead.
            return null;
        }
    }

    private byte[] download(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void saveToDisk(Path file, byte[] bytes) {
        try {
            Files.createDirectories(cacheDir);
            Path temp = Files.createTempFile(cacheDir, "cover", ".tmp");
            Files.write(temp, bytes);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // The cover still shows; it will just be downloaded again next time.
        }
    }

    /** Converts to a format that draws quickly and caps the width, halving in steps so it stays sharp. */
    private static BufferedImage shrink(BufferedImage image) {
        int targetWidth = Math.min(MAX_WIDTH, image.getWidth());
        BufferedImage current = image;
        do {
            int width = Math.max(targetWidth, current.getWidth() / 2);
            int height = Math.max(1, Math.round(current.getHeight() * (width / (float) current.getWidth())));
            current = redraw(current, width, height);
        } while (current.getWidth() > targetWidth);
        return current;
    }

    private static BufferedImage redraw(BufferedImage source, int width, int height) {
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return copy;
    }

    private static String fileNameFor(String url) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash) + ".img";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available in Java", e);
        }
    }
}
