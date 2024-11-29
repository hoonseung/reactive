package nio;

import java.io.IOException;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaNIOMultiClient {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(50);
    private static final List<CompletableFuture<Void>> COMPLETABLE_FUTURES = new ArrayList<>();
    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0L);

    @SneakyThrows
    public static void main(String[] args) {
        log.info("cpu count: {}", Runtime.getRuntime().availableProcessors());
        log.info("start main");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                    InetSocketAddress socketAddress = new InetSocketAddress("localhost", 8080);
                    try (SocketChannel socketChannel = SocketChannel.open(socketAddress)) {

                        ByteBuffer writeBuffer = ByteBuffer.wrap("im client".getBytes());
                        socketChannel.write(writeBuffer);
                        writeBuffer.clear();

                        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

                        while (socketChannel.read(readBuffer) > 0) {
                            readBuffer.flip();
                            String res = StandardCharsets.UTF_8.decode(readBuffer).toString();
                            log.info("res: {}", res);
                            readBuffer.clear();
                        }

                        ATOMIC_LONG.incrementAndGet();
                    } catch (IOException e) {
                        log.error("e", e);
                    }
                }, EXECUTOR_SERVICE);

            COMPLETABLE_FUTURES.add(voidCompletableFuture);
        }
        CompletableFuture.allOf(COMPLETABLE_FUTURES.toArray(new CompletableFuture[0])).join();
        EXECUTOR_SERVICE.shutdown();
        log.info("end main");
        long end = System.currentTimeMillis() - start;
        log.info("total during time: {}s", end / 1000.0);
        log.info("count: {}", ATOMIC_LONG.intValue());
    }
}
