package io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaIOMultiClient {

    private static ExecutorService executors = Executors.newFixedThreadPool(50);
    private static List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
    private static AtomicLong atomicLong = new AtomicLong(0L);

    @SneakyThrows
    public static void main(String[] args) {
        log.info("cpu count: {}", Runtime.getRuntime().availableProcessors());
        log.info("start main");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                try (Socket socket = new Socket("localhost", 8080);
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();) {
                    out.write("hello server".getBytes());
                    out.flush();

                    byte[] bytes = new byte[1024];
                    in.read(bytes);
                    String res = new String(bytes, Charset.defaultCharset()).trim();
                    log.info("res: {}", res);

                    atomicLong.incrementAndGet();
                } catch (Exception e) {
                }
            }, executors);

            completableFutures.add(voidCompletableFuture);
        }

        executors.shutdown();
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        log.info("end main");
        long end = System.currentTimeMillis() - start;
        log.info("total during time: {}s", end / 1000.0);
        log.info("count: {}", atomicLong.intValue());
    }
}
