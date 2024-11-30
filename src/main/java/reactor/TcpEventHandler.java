package reactor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TcpEventHandler implements EventHandler {

    private static final ExecutorService executor = Executors.newFixedThreadPool(50);
    private final SocketChannel clientSocket;

    @SneakyThrows
    public TcpEventHandler(Selector selector, SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
        this.clientSocket.configureBlocking(false);
        this.clientSocket.register(selector, SelectionKey.OP_READ).attach(this);
    }


    @Override
    public void handle() {
        String requestBody = requestWorker(clientSocket);
        log.info("{}", requestBody);
        sendResponse(clientSocket, requestBody);
    }


    @SneakyThrows
    private String requestWorker(SocketChannel clientSocket) {
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

        clientSocket.read(readBuffer);
        readBuffer.flip();
        return StandardCharsets.UTF_8.decode(readBuffer).toString();
    }

    @SneakyThrows
    private void sendResponse(SocketChannel clientSocket, String requestBody) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                String content = "received: " + requestBody;
                ByteBuffer writeBuffer = ByteBuffer.wrap(content.getBytes());
                clientSocket.write(writeBuffer);
                clientSocket.close();
            } catch (Exception e) {
                log.error("e", e);
            }
        }, executor);
    }
}
