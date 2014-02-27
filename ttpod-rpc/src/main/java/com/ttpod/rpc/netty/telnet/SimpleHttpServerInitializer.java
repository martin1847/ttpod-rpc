package com.ttpod.rpc.netty.telnet;

import com.ttpod.rpc.server.ServerProcessor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * date: 14-2-27 下午6:35
 *
 * @author: yangyang.cong@ttpod.com
 */
public class SimpleHttpServerInitializer extends ChannelInitializer<SocketChannel> {
    final Map<String,ServerProcessor> processors =  new HashMap<>();

    public void setProcessors(Map<String, ServerProcessor> processors) {
        this.processors .putAll( processors);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
//        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("handler", new SimpleHttpServerHandler(processors));
        // and then business logic.
    }
}

