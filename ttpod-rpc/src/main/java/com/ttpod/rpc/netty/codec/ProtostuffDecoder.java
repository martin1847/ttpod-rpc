package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.IdStrategy;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * date: 14-3-21 下午4:40
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public abstract class ProtostuffDecoder<DataBean> extends MessageToMessageDecoder<ByteBuf> {

    protected static final Logger logger = LoggerFactory.getLogger(ProtostuffDecoder.class);

    public static final IdStrategy ID_STRATEGY = new DefaultIdStrategy();
    public static final Schema<ResponseBean> RESPONSE_SCHEMA =  RuntimeSchema.getSchema(ResponseBean.class, ID_STRATEGY);
    public static final Schema<RequestBean> REQUEST_SCHEMA =  RuntimeSchema.getSchema(RequestBean.class,ID_STRATEGY);


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int offset;
//        try {
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
            DataBean pojo = newBean();
            ProtostuffIOUtil.mergeFrom(array, offset, length, pojo, cachedSchema());
            out.add(pojo);
//        }finally {//TODO 20140820 Mem Leak ?
//             logger.debug("ProtostuffDecoder release ByteBuf {} ",msg.release());
//        }
    }

    abstract protected DataBean newBean();

    abstract protected Schema<DataBean> cachedSchema();


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("MayBe Required [pojo class] NOT Found For decode .", cause);
        super.exceptionCaught(ctx, cause);
    }

}
