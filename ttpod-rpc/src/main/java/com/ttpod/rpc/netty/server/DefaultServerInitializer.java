package com.ttpod.rpc.netty.server;

import com.ttpod.rpc.netty.codec.StringReqDecoder;
import com.ttpod.rpc.netty.codec.ResponseEncoder;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * date: 14-2-12 下午4:26
 *
 * @author: yangyang.cong@ttpod.com
 */
public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {

//    final ChannelHandler frameEncoder = new ProtobufVarint32LengthFieldPrepender();
    final ChannelHandler frameEncoder = new LengthFieldPrepender(4);
    final ChannelHandler responseEncoder = new ResponseEncoder();

    // TODO USE  LocalEventLoop ?
    final EventLoopGroup serverGroup = new NioEventLoopGroup(
//                0, Executors.newCachedThreadPool()
    );

    final DefaultServerHandler serverHandler;

    public DefaultServerInitializer(DefaultServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast("decoder", new StringReqDecoder());

        p.addLast("frameEncoder",frameEncoder );
        p.addLast("responseEncoder", responseEncoder);

        p.addLast(serverGroup, "serverHandler", serverHandler);
    }
}
