package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.IdStrategy;
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
    public static final IdStrategy ID_STRATEGY = new DefaultIdStrategy();

//    static {
//        ExplicitIdStrategy.Registry r = new ExplicitIdStrategy.Registry();
//        r.registerCollection(CollectionSchema.MessageFactories.ArrayList, 1)
//                .registerCollection(CollectionSchema.MessageFactories.HashSet, 2)
//                .registerCollection(CollectionSchema.MessageFactories.LinkedList, 3)
//                .registerCollection(CollectionSchema.MessageFactories.TreeSet, 4)
//                .registerCollection(CollectionSchema.MessageFactories.CopyOnWriteArrayList, 5);
//
//        r.registerMap(MapSchema.MessageFactories.HashMap, 1)
//                .registerMap(MapSchema.MessageFactories.LinkedHashMap, 2)
//                .registerMap(MapSchema.MessageFactories.TreeMap, 3)
//                .registerMap(MapSchema.MessageFactories.Hashtable, 4)
//        .registerMap(MapSchema.MessageFactories.ConcurrentHashMap, 5);
//
//        r.registerPojo(ResponseBean.class, 1)
//        .registerPojo(RequestBean.class, 2)
//        .registerPojo(NullPointerException.class, 3)
//                .registerPojo(IOException.class, 4)
//                .registerPojo(Throwable.class, 5)
//                .registerPojo(Exception.class, 6)
//                .registerPojo(RuntimeException.class, 7);
//
//        ID_STRATEGY = new DefaultIdStrategy(r.strategy,1);
//    }

    //https://code.google.com/p/protostuff/wiki/ProtostuffRuntime#Performance_guidelines

    static final Schema<ResponseBean> schema =  RuntimeSchema.getSchema(ResponseBean.class,ID_STRATEGY);

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