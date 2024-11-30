package common;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MsgCodec {

    private static final StringBuilder sb = new StringBuilder();

    public static ByteBuffer encode(final String msg) {
        return StandardCharsets.UTF_8.encode(responseBuild(msg));
    }

    public static String decode(final ByteBuffer byteBuffer) {
        byteBuffer.flip();
        String httpRequest = StandardCharsets.UTF_8.decode(byteBuffer).toString().trim();

        String[] messageLine = httpRequest.split("\n");
        String path = messageLine[0].split(" ")[1];

        URI uri = URI.create(path);
        String query = uri.getQuery() == null ? "" : uri.getQuery();

        Map<String, String> queryMap = Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(s -> s.length == 2)
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        return queryMap.getOrDefault("name", "World");
    }


    private static String responseBuild(String msg) {
        String body = "<html><body><h1>" + msg + "</h1></body></html>";

        sb.append("HTTP/1.1 200 OK\r\n")
                .append("Content-Type: text/html; charset=utf-8\r\n")
                .append("Content-Length: ").append(body.getBytes(StandardCharsets.UTF_8).length).append("\r\n")
                .append("\r\n")
                .append(body);

        return sb.toString();
    }


}
