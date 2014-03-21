package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.Schema;
import com.ttpod.rpc.ResponseBean;
import io.netty.channel.ChannelHandler;

/**
 * date: 14-2-7 下午12:00
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public final class ResponseDec extends ProtostuffDecoder<ResponseBean> {


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


    @Override
    protected final ResponseBean newBean() {
        return new ResponseBean();
    }

    @Override
    protected final Schema<ResponseBean> cachedSchema() {
        return RESPONSE_SCHEMA;
    }

}