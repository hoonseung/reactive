package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHttpServer {
    @SneakyThrows
    public static void main(String[] args) {
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup(4);
        DefaultEventLoopGroup eventExecutorsGroup = new DefaultEventLoopGroup(4);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(eventExecutorsGroup, new LoggingHandler(LogLevel.INFO));
                            channel.pipeline()
                                    .addLast(
                                            new HttpServerCodec(),
                                            new HttpObjectAggregator(1024 * 1024),
                                            new NettyHttpServerHandler()
                                    );
                        }
                    })
                    .bind(new InetSocketAddress("localhost", 8080))
                    .sync()
                    .addListener(new FutureListener<>() {
                        @Override
                        public void operationComplete(Future<Void> future) throws Exception {
                            if (future.isSuccess()) {
                                log.info("Success to bind 8080");
                            } else {
                                log.info("Failed to bind 8080");
                            }
                        }
                    }).channel().closeFuture().sync();
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            eventExecutorsGroup.shutdownGracefully();
        }
    }
}
