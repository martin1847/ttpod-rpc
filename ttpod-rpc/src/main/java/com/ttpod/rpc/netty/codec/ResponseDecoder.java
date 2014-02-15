package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.ttpod.rpc.ResponseBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * date: 14-2-7 下午12:00
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public class ResponseDecoder extends MessageToMessageDecoder<ByteBuf> {

    static final Logger logger = LoggerFactory.getLogger(ResponseDecoder.class);
    static final Schema<ResponseBean> schema =  RuntimeSchema.getSchema(ResponseBean.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int offset;
        final int length = msg.readableBytes();
        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = new byte[length];
            msg.getBytes(msg.readerIndex(), array, 0, length);
            offset = 0;
        }
        // deser
        ResponseBean pojo = new ResponseBean();
        ProtostuffIOUtil.mergeFrom(array, offset, length, pojo, schema);
        out.add(pojo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("MayBe Required [pojo class] NOT Found For decode .", cause);
        super.exceptionCaught(ctx, cause);
    }
}