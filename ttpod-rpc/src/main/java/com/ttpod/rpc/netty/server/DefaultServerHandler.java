package com.ttpod.rpc.netty.server;

import com.ttpod.rpc.InnerBindUtil;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.server.ServerHandler;
import com.ttpod.rpc.server.ServerProcessor;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * date: 14-2-9 下午1:11
 *
 * @author: yangyang.cong@ttpod.com
 */
@ChannelHandler.Sharable
public class DefaultServerHandler extends SimpleChannelInboundHandler<RequestBean>  implements ServerHandler {
    static final Logger logger = LoggerFactory.getLogger(DefaultServerHandler.class);

    protected void messageReceived(ChannelHandlerContext ctx, RequestBean msg) throws Exception {
        short reqId = InnerBindUtil.id(msg);
        ResponseBean data = handleRequest(msg);
        InnerBindUtil.bind(data,reqId);
//        data.setReqId(msg._reqId);
//        data.setCode(1);
//        data.setPages(10);
//        data.setRows(2000);
//        data.setData(Arrays.asList(new Pojo(q, 100), new Pojo("OK", 10)));
        ChannelFuture future = ctx.writeAndFlush(data);
        //  Close the connection if the client has sent 'bye'.
//        if ("bye".equals(q)) {
//            future.addListener(ChannelFutureListener.CLOSE);
//        }
    }

    protected void channelRead0(ChannelHandlerContext ctx, RequestBean msg) throws Exception {
        messageReceived(ctx,msg);
    }

    ServerProcessor[] processors;
    public void setProcessors(ServerProcessor[] processors) {
        this.processors = processors;
    }


    public ResponseBean handleRequest(RequestBean request) throws Exception{
        try{
            return   processors[request.getService()].handle(request);
        }catch (Throwable e){//For RPC must Notify Client .
            logger.error("handleRequest Error ",e);
            ResponseBean res = ResponseBean.error();
            res.setData(e.getLocalizedMessage());
            return res;
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("exceptionCaught From {} " , ctx.channel().remoteAddress());
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive From {} " , ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive From {} " , ctx.channel().remoteAddress());
        ctx.close();
        super.channelInactive(ctx);
    }
}
