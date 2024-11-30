package selector;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SelectorMultiClient {

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
                try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 8080))) {
                    ByteBuffer writeBuffer = ByteBuffer.wrap("hello server".getBytes());
                    socketChannel.write(writeBuffer);
                    writeBuffer.clear();

                    ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
                    while (socketChannel.read(readBuffer) > 0) {
                        readBuffer.flip();
                        String res = StandardCharsets.UTF_8.decode(readBuffer).toString();
                        readBuffer.clear();
                        log.info("{}", res);
                    }

                    atomicLong.incrementAndGet();
                } catch (Exception e) {
                    log.error("e", e);
                }
            }, executors);

            completableFutures.add(voidCompletableFuture);
        }

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        executors.shutdown();
        log.info("end main");
        long end = System.currentTimeMillis() - start;
        log.info("total during time: {}s", end / 1000.0);
        log.info("count: {}", atomicLong.intValue());
    }
}
