package selector;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorMultiServer {

    private static ExecutorService executor = Executors.newFixedThreadPool(50);

    @SneakyThrows
    public static void main(String[] args) {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open(); Selector selector = Selector.open()) {
            serverSocket.bind(new InetSocketAddress("localhost", 8080));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                if (selectionKeys.hasNext()) {
                    SelectionKey key = selectionKeys.next();
                    selectionKeys.remove();

                    if (key.isAcceptable()) {

                        SocketChannel clientSocket = serverSocket.accept();
                        clientSocket.configureBlocking(false);
                        clientSocket.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel clientSocket = (SocketChannel) key.channel();

                        sendResponse(clientSocket, requestWorker(clientSocket));
                    }
                }
            }
        }
    }

    @SneakyThrows
    private static String requestWorker(SocketChannel clientSocket) {
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

        clientSocket.read(readBuffer);

        readBuffer.flip();
        String requestBody = StandardCharsets.UTF_8.decode(readBuffer).toString();
        log.info("requestBody: {}", requestBody);

        return requestBody;

    }

    @SneakyThrows
    private static void sendResponse(SocketChannel clientSocket, String requestBody) {
        CompletableFuture.runAsync(()-> {
            try {
                Thread.sleep(100);
                String content = "received: " + requestBody;
                ByteBuffer writeBuffer = ByteBuffer.wrap(content.getBytes());
                clientSocket.write(writeBuffer);
                clientSocket.close();
            }catch (Exception e){}
        }, executor);
    }

}
