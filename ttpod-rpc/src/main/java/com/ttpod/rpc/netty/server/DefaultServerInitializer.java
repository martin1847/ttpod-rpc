package com.ttpod.rpc.netty.server;

import com.ttpod.rpc.netty.codec.*;
import io.netty.channel.*;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * date: 14-2-12 下午4:26
 *
 * @author: yangyang.cong@ttpod.com
 */
public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {

//    final ChannelHandler frameEncoder = new ProtobufVarint32LengthFieldPrepender();
    final ChannelHandler frameEnc = new LengthFieldPrepender(4);

    final ChannelHandler requestDec = new RequestDec();
    final ChannelHandler responseEnc = new ResponseEnc();

    final DefaultServerHandler serverHandler;

    public DefaultServerInitializer(DefaultServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    final EventLoopGroup serverHandlerLoop = new LocalEventLoopGroup();

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        if(StringReqEnc.disable){
            p.addLast("frameDec", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
            p.addLast("requestDec", requestDec);
        }else{
            p.addLast("stringReqDec", new StringReqDec());
        }

        p.addLast("frameEnc",frameEnc );
        p.addLast("responseEnc", responseEnc);

        p.addLast(serverHandlerLoop, "serverHandler", serverHandler);
    }
}
