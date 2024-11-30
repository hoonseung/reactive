package reactor;

import common.MsgCodec;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpEventHandler implements EventHandler{

    private static ExecutorService executor = Executors.newFixedThreadPool(50);
    private final SocketChannel clientSocket;


    @SneakyThrows
    public HttpEventHandler(Selector selector, SocketChannel clientSocket) {
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
        return MsgCodec.decode(readBuffer);
    }

    @SneakyThrows
    private void sendResponse(SocketChannel clientSocket, String requestBody) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                ByteBuffer writeBuffer = MsgCodec.encode(requestBody);
                clientSocket.write(writeBuffer);
                clientSocket.close();
            } catch (Exception e) {
                log.error("e", e);
            }
        }, executor);
    }
}
