package nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class JavaNIONonblockingServer {

    @SneakyThrows
    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", 8080));
            serverSocketChannel.configureBlocking(false);

            while (true) {
                SocketChannel clientSocket = serverSocketChannel.accept();
                if (Objects.isNull(clientSocket)) {
                    Thread.sleep(1000);
                    log.info("waiting accept");
                    continue;
                }
                CompletableFuture.runAsync(() -> requestWorker(clientSocket));
            }
        }
    }


    private static void requestWorker(SocketChannel clientSocket) {
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
        try {
            while (clientSocket.read(readBuffer) == 0) {
                log.info("waiting for client data read");
            }
            readBuffer.flip();
            String data = StandardCharsets.UTF_8.decode(readBuffer).toString();
            log.info("request data: {}", data);

            ByteBuffer writeBuffer = ByteBuffer.wrap("im server".getBytes());
            clientSocket.write(writeBuffer);
            clientSocket.close();
        } catch (IOException e) {
            log.error("e", e);
        }

    }

}
