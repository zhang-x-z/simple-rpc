package cn.edu.nju.zxz.simplerpc.network.impl.netty;

import cn.edu.nju.zxz.simplerpc.SimpleRPCServiceRegister;
import cn.edu.nju.zxz.simplerpc.config.ConfigConstant;
import cn.edu.nju.zxz.simplerpc.config.SimpleRPCConfig;
import cn.edu.nju.zxz.simplerpc.network.RPCServer;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.codec.RPCDataDecoder;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.codec.RPCDataEncoder;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.dto.NettyRPCRequest;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.dto.NettyRPCRespond;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Objects;

public class NettyRPCServer implements RPCServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRPCServer.class);

    @Override
    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            SslContext sslContext;
            try {
                KeyStore keyStore = KeyStore.getInstance(
                        SimpleRPCConfig.getConfig(ConfigConstant.SSL_SERVER_KEYSTORE_TYPE)
                );
                keyStore.load(
                        Files.newInputStream(Paths.get(SimpleRPCConfig.getConfig(ConfigConstant.SSL_SERVER_KEYSTORE_PATH))),
                        SimpleRPCConfig.getConfig(ConfigConstant.SSL_SERVER_KEYSTORE_STOREPASS).toCharArray()
                );
                KeyManagerFactory keyManager = KeyManagerFactory.getInstance(
                        SimpleRPCConfig.getConfig(ConfigConstant.SSL_SERVER_KEY_MANAGER_TYPE)
                );
                keyManager.init(keyStore, SimpleRPCConfig.getConfig(ConfigConstant.SSL_SERVER_KEYSTORE_KEYPASS).toCharArray());
                sslContext = SslContextBuilder.forServer(keyManager).build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure SSL in server", e);
            }
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addFirst(new SslHandler(sslContext.newEngine(ch.alloc())))
                                    .addLast(new RPCDataEncoder()).addLast(new RPCDataDecoder()).addLast(
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            try {
                                                NettyRPCRespond nettyRPCRespond = new NettyRPCRespond();
                                                if (msg instanceof NettyRPCRequest) {
                                                    nettyRPCRespond.setId(((NettyRPCRequest) msg).getId());
                                                    callMethod((RPCRequest) msg, nettyRPCRespond);
                                                } else {
                                                    nettyRPCRespond.setSucceed(false);
                                                }
                                                ctx.writeAndFlush(nettyRPCRespond);
                                                ctx.close();
                                            } finally {
                                                ReferenceCountUtil.release(msg);
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                            LOGGER.error("NettyRPCServer Handler error.", cause);
                                            ctx.close();
                                        }
                                    }
                            );
                        }
                    });
            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void callMethod(RPCRequest request, RPCRespond respond) {
        try {
            Object[] serviceAndToken = SimpleRPCServiceRegister.getRpcServiceMap().get(request.getInterfaceName());
            if (serviceAndToken == null || serviceAndToken.length == 0 || serviceAndToken[0] == null) {
                throw new RuntimeException("Method [" + request.getMethodName() + "] of interface [" + request.getInterfaceName() + "] has not registered in server.");
            }
            Object service = serviceAndToken[0];
            String token = (String) serviceAndToken[1];
            if (!Objects.equals(token, request.getToken())) {
                throw new RuntimeException("Wrong token. Client can't access to the interface [" + request.getInterfaceName() + "]");
            }
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object res = method.invoke(service, request.getParameters());
            respond.setSucceed(true);
            respond.setRespond(res);
            respond.setErrorMessage(null);
        } catch (Exception e) {
            LOGGER.error("Failed to invoke method [" + request.getMethodName() + "] of interface [" + request.getInterfaceName() + "].", e);
            respond.setSucceed(false);
            respond.setRespond(null);
            respond.setErrorMessage("Remote error message: [" + e.getMessage() + "].");
        }
    }
}
