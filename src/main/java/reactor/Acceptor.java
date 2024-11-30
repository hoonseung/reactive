package reactor;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import lombok.SneakyThrows;

public class Acceptor implements EventHandler {

    private final Selector selector;
    private final ServerSocketChannel serverSocket;

    public Acceptor(Selector selector, ServerSocketChannel serverSocket) {
        this.selector = selector;
        this.serverSocket = serverSocket;
    }

    @SneakyThrows
    @Override
    public void handle() {
        SocketChannel clientSocket = serverSocket.accept();
        new HttpEventHandler(selector, clientSocket);
    }
}
