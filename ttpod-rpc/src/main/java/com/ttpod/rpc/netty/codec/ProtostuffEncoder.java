package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.ttpod.rpc.ResponseBean;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * date: 14-2-7 下午12:01
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public abstract class ProtostuffEncoder<DataBean> extends MessageToMessageEncoder<DataBean> {

    static final Logger logger = LoggerFactory.getLogger(ProtostuffEncoder.class);
    @Override
    protected void encode(
            ChannelHandlerContext ctx, DataBean msg, List<Object> out) throws Exception {
//        ExplicitIdStrategy.Registry.
        int bufferSize = bufferSize();
        byte[] data = ProtostuffIOUtil.toByteArray(msg, cachedSchema(), LinkedBuffer.allocate(bufferSize));

        if( data.length > bufferSize){
            logger.info("encode {} bytes: {} > bufferSize",msg.getClass().getSimpleName() , data.length);
        }
        out.add(wrappedBuffer(data));
    }
    abstract protected Schema<DataBean> cachedSchema();

    abstract protected int bufferSize();
}