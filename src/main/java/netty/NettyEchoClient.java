package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;

public class NettyEchoClient {
    @SneakyThrows
    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            var client = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(
                                            new LoggingHandler(LogLevel.INFO),
                                            new StringDecoder(),
                                            new StringEncoder(),
                                            new NettyEchoClientHandler());
                        }
                    });

            client.connect(new InetSocketAddress("localhost", 8080)).sync()
                    .channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
