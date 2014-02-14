package com.ttpod.rpc.netty.client;

import com.ttpod.rpc.netty.codec.RequestEncoder;
import com.ttpod.rpc.netty.codec.ResponseDecoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * DefaultClientInitializer
 *
 * date: 14-2-12 下午4:24
 *
 * @author: yangyang.cong@ttpod.com
 */
public class DefaultClientInitializer extends ChannelInitializer<SocketChannel> {

    final ChannelHandler requestEncoder = new RequestEncoder();
    final ChannelHandler responseDecoder = new ResponseDecoder();

    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
//        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
        p.addLast("responseDecoder", responseDecoder);
        p.addLast("requestEncoder", requestEncoder);
        initClientHandler(p);
    }

    protected void  initClientHandler(ChannelPipeline p){
        p.addLast(new DefaultClientHandler());
    }
}
