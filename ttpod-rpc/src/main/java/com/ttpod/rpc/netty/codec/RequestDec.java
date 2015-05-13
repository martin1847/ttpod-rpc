package com.ttpod.rpc.netty.codec;

//import com.dyuproject.protostuff.Schema;
import com.ttpod.rpc.RequestBean;
import io.netty.channel.ChannelHandler;
import io.protostuff.Schema;

/**
 * date: 14-3-21 下午4:51
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public final class RequestDec extends ProtostuffDecoder<RequestBean> {
    @Override
    protected RequestBean newBean() {
        return new RequestBean();
    }

    @Override
    protected Schema<RequestBean> cachedSchema() {
        return REQUEST_SCHEMA;
    }
}
