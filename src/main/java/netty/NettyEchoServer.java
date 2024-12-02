package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyEchoServer {
    @SneakyThrows
    public static void main(String[] args) {
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup(4);
        DefaultEventLoopGroup eventExecutorGroup = new DefaultEventLoopGroup(4);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            var server = serverBootstrap
                    .group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(eventExecutorGroup, new LoggingHandler(LogLevel.INFO));

                            channel.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new NettyEchoServerHandler()
                            );
                        }
                    });
            server.bind(new InetSocketAddress("localhost", 8080)).sync()
                    .addListener(new FutureListener<>() {
                        @Override
                        public void operationComplete(Future<Void> voidFuture) throws Exception {
                            if (voidFuture.isSuccess()) {
                                log.info("Success to bind 8080");
                            } else {
                                log.info("Failed to bind 8080");
                            }
                        }
                    })
                    .channel().closeFuture().sync();
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            eventExecutorGroup.shutdownGracefully();
        }
    }
}
