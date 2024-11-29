package io;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class JavaIOSingleServer {
    @SneakyThrows
    public static void main(String[] args) {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));


        while (true) {
            Socket clientSocket = serverSocket.accept();
            try (
                    InputStream in = clientSocket.getInputStream();
                    OutputStream out = clientSocket.getOutputStream()) {

                byte[] bytes = new byte[1024];
                in.read(bytes);
                log.info("request: {}", new String(bytes).trim());

                String response = "im server";
                out.write(response.getBytes(UTF_8));
                out.flush();
            } catch (IOException e) {
                log.error("e", e);
            }
        }
    }
}
