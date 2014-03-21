package com.ttpod.rpc.netty.codec;

import com.dyuproject.protostuff.Schema;
import com.ttpod.rpc.ResponseBean;
import io.netty.channel.ChannelHandler;

/**
 * date: 14-2-7 下午12:01
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public final class ResponseEnc extends ProtostuffEncoder<ResponseBean> {

    /**
     *
     *http://code.google.com/p/protostuff/wiki/ProtostuffRuntime
     *
     * http://code.google.com/p/protostuff/source/browse/trunk/protostuff-runtime-registry/src/test/java/com/dyuproject/protostuff/runtime/ExplicitRuntimeObjectSchemaTest.java
     *
     *
     * Performance guidelines
     As much as possible, use the concrete type when declaring a field.

     For polymorhic datasets, prefer abstract classes vs interfaces.

     Use ExplicitIdStrategy to write the type metadata as int (ser/deser will be faster and the serialized size will be smaller).
     Register your concrete classes at startup via ExplicitIdStrategy.Registry.
     For objects not known ahead of time, use IncrementalIdStrategy
     You can activate it using the system property:
     -Dprotostuff.runtime.id_strategy_factory=com.dyuproject.protostuff.runtime.IncrementalIdStrategy$Factory
     You can also use these strategies independently. E.g:
     final IncrementalIdStrategy strategy = new IncrementalIdStrategy(....);
     // use its registry if you want to pre-register classes.

     // Then when your app needs a schema, use it.
     RuntimeSchema.getSchema(clazz, strategy);
     DEFAULT_JVM_OPTS="-DResponseEncoder.buffer=32768"
     */

    static final int BUFFER_SIZE = Integer.getInteger(ResponseEnc.class.getSimpleName()+ ".buffer",1024);

    @Override
    protected Schema<ResponseBean> cachedSchema() {
        return ProtostuffDecoder.RESPONSE_SCHEMA;
    }

    @Override
    protected int bufferSize() {
        return BUFFER_SIZE;
    }
}