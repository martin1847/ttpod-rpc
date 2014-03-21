package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.Schema;
import com.ttpod.rpc.RequestBean;
import io.netty.channel.ChannelHandler;

/**
 * date: 14-3-21 下午5:36
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public final class RequestEnc extends ProtostuffEncoder<RequestBean> {

    static final int BUFFER_SIZE = Integer.getInteger(RequestEnc.class.getSimpleName() + ".buffer",1024);

    @Override
    protected Schema<RequestBean> cachedSchema() {
        return ProtostuffDecoder.REQUEST_SCHEMA;
    }

    @Override
    protected int bufferSize() {
        return BUFFER_SIZE;
    }
}
