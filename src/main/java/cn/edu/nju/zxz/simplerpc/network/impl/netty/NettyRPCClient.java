package cn.edu.nju.zxz.simplerpc.network.impl.netty;

import cn.edu.nju.zxz.simplerpc.config.ConfigConstant;
import cn.edu.nju.zxz.simplerpc.config.SimpleRPCConfig;
import cn.edu.nju.zxz.simplerpc.network.RPCClient;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.codec.RPCDataDecoder;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.codec.RPCDataEncoder;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.dto.NettyRPCRequest;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.dto.NettyRPCRespond;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyRPCClient implements RPCClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRPCClient.class);
    private final Bootstrap bootstrap;
    private final Map<String, CompletableFuture<RPCRespond>> respondFutureMap;
    private final int respondTimeoutMS;

    public NettyRPCClient() {
        this.respondFutureMap = new ConcurrentHashMap<>();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        SslContext sslContext;
        try {
            KeyStore keyStore = KeyStore.getInstance(SimpleRPCConfig.getConfig(ConfigConstant.SSL_CLIENT_KEYSTORE_TYPE));
            keyStore.load(Files.newInputStream(Paths.get(
                    SimpleRPCConfig.getConfig(ConfigConstant.SSL_CLIENT_TRUST_KEYSTORE_PATH))),
                    SimpleRPCConfig.getConfig(ConfigConstant.SSL_CLIENT_TRUST_KEYSTORE_STOREPASS).toCharArray()
            );
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance(
                    SimpleRPCConfig.getConfig(ConfigConstant.SSL_CLIENT_TRUST_MANAGER_TYPE)
            );
            trustManager.init(keyStore);
            sslContext = SslContextBuilder.forClient().trustManager(trustManager).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure SSL in client.", e);
        }
        int connectTimeoutMS;
        try {
            connectTimeoutMS = Integer.parseInt(SimpleRPCConfig.getConfig(ConfigConstant.CLIENT_CONNECT_TIMEOUT_MS));
            this.respondTimeoutMS = Integer.parseInt(SimpleRPCConfig.getConfig(ConfigConstant.CLIENT_RESPOND_TIMEOUT_MS));
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure client timeout options.", e);
        }
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMS)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addFirst(new SslHandler(sslContext.newEngine(ch.alloc())))
                                .addLast(new RPCDataEncoder()).addLast(new RPCDataDecoder()).addLast(
                                new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        try {
                                            if (!(msg instanceof NettyRPCRespond)) {
                                                throw new RuntimeException("Type of respond is Not NettyRPCRespond.");
                                            }
                                            String id = ((NettyRPCRespond) msg).getId();
                                            CompletableFuture<RPCRespond> future;
                                            if (id == null || (future = respondFutureMap.get(id)) == null) {
                                                throw new RuntimeException("Can not find request with id [" + id + "].");
                                            }
                                            future.complete((RPCRespond) msg);
                                            ctx.close();
                                        } finally {
                                            ReferenceCountUtil.release(msg);
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        LOGGER.error("NettyRPCClient Handler error.", cause);
                                        ctx.close();
                                    }
                                }
                        );
                    }
                });
    }

    @Override
    public RPCRespond sendRPCData(String serverAddress, RPCRequest request) throws Exception {
        String[] addr = serverAddress.split(":");
        if (addr.length != 2) {
            throw new RuntimeException("Server Address: [" + serverAddress + "] is wrong. It should be [HOST]:[PORT]");
        }
        int port;
        try {
            port = Integer.parseInt(addr[1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("The Port of Server Address: [" + serverAddress + "] is wrong. It should be an integer.");
        }
        CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
        bootstrap.connect(addr[0], port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                LOGGER.info("Successfully connect to [" + serverAddress + "].");
                channelFuture.complete(future.channel());
            } else {
                LOGGER.error("Connecting to [" + serverAddress + "] failed.");
                channelFuture.completeExceptionally(future.cause());
            }
        });
        NettyRPCRequest nettyRequest = new NettyRPCRequest(request);
        String id = UUID.randomUUID().toString();
        nettyRequest.setId(id);
        CompletableFuture<RPCRespond> respondFuture = new CompletableFuture<>();
        respondFutureMap.put(id, respondFuture);
        channelFuture.get().writeAndFlush(nettyRequest).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOGGER.error("Sending to server [" + serverAddress + "] failed.");
                respondFutureMap.remove(id);
                future.channel().close();
                respondFuture.completeExceptionally(future.cause());
            }
        });
        // Add timeout
        // TODO: close channel after timeout
        return respondFuture.get(this.respondTimeoutMS, TimeUnit.MILLISECONDS);
    }
}
