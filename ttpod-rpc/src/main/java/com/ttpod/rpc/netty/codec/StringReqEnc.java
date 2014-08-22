package com.ttpod.rpc.netty.codec;

import com.ttpod.rpc.InnerBindUtil;
import com.ttpod.rpc.RequestBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * date: 14-1-28 上午11:57
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public class StringReqEnc extends MessageToByteEncoder<RequestBean<String>> {


    public static final Boolean disable = Boolean.getBoolean("StringReqEnc.disable");

    static final int BYTE_FIELDS = 1*2 + 3 * 1;// _id + 3 byte
    public static final short MAGIC  = 0XCF;

    public StringReqEnc() {
        super(false);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, @SuppressWarnings("unused") RequestBean<String> msg,
                                     boolean preferDirect) throws Exception {
//        if (preferDirect) {
//            return ctx.alloc().ioBuffer();
//        } else {
            return ctx.alloc().heapBuffer();
//        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestBean<String> req, ByteBuf out) throws Exception {

//        System.out.println("[QueryReqEncoder call]   ");
//        byte[] q = ;
//        byte[] full = new byte[q.length + BYTE_FIELDS];
        byte[] string = req.getData().getBytes(CharsetUtil.UTF_8);
        out.writeByte(MAGIC);
        out.writeShort(string.length + BYTE_FIELDS);
        out.writeShort( InnerBindUtil.id(req));
        out.writeByte(  req.getService());
        out.writeByte(  req.getPage());
        out.writeByte(  req.getSize());
        out.writeBytes(string);
//        System.arraycopy(q,0,full,q.length -1, q.length);
//        out.add(Unpooled.wrappedBuffer(full));
//        System.out.println("[QueryReqEncoder end] : "+ msg);
       // out.add(ByteBufUtil.encodeString(buf, CharBuffer.wrap(msg.getData()),QueryReqDecoder.UTF8));
    }
}
