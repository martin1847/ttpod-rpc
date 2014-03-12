package com.ttpod.rpc.netty.client;

import com.ttpod.rpc.InnerBindUtil;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.client.OutstandingContainer;
import com.ttpod.rpc.client.ResponseObserver;
import com.ttpod.rpc.pool.ChannelPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * date: 14-2-7 下午1:16
 *
 * @author: yangyang.cong@ttpod.com
 */
public class DefaultClientHandler extends SimpleChannelInboundHandler<ResponseBean> implements ClientHandler {
    // Stateful properties
    private volatile Channel channel;
    private static final Logger logger = LoggerFactory.getLogger(DefaultClientHandler.class);

    private final OutstandingContainer outstandings;


    public DefaultClientHandler(){
        this(new OutstandingContainer.Array());
    }
    public DefaultClientHandler(OutstandingContainer container){
        this.outstandings = container;
    }

    protected void messageReceived(ChannelHandlerContext ctx, ResponseBean msg) throws Exception {
        ResponseObserver observer = outstandings.remove(InnerBindUtil.id(msg));
        if(null != observer){
            observer.onSuccess(msg);
        }else{
            logger.error("Unknown ResponseBean with id : {}", InnerBindUtil.id(msg));
        }
    }

    @Deprecated
    protected void channelRead0(ChannelHandlerContext ctx, ResponseBean msg) throws Exception{
        messageReceived(ctx,msg);
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    public void rpc(final RequestBean req, ResponseObserver observer) {
        if(null !=  outstandings.put(InnerBindUtil.bind( req, outstandings.nextId()),observer)){
            logger.warn("rpc req id Conflict : {}" ,req);
        }
        channel.writeAndFlush(req);
        // !future.isSuccess() clean -> Ring Buffer ( just waring with put above )?
//        .addListener( new ChannelFutureListener() {
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if(!future.isSuccess()){
//                    outstandings.remove(req._reqId);
//                }
//            }
//        });
    }

    @Override
    public ResponseBean rpc(RequestBean req) {
        ResponseObserver.Blocking done = new ResponseObserver.Blocking();
        rpc(req,done);
        return done.get();
    }

    @Override
    public ResponseBean rpc(RequestBean req, int timeOutMills) throws TimeoutException{
        ResponseObserver.Future future = new ResponseObserver.Future();
        rpc(req,future);
        try {
            return future.get(timeOutMills, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(" rpc future Error -> ",e);
            throw new RuntimeException("future Got Error . ",e);
        }
    }

    public void setChannelPool(ChannelPool<ClientHandler> channelPool) {
        this.channelPool = channelPool;
    }

    ChannelPool<ClientHandler> channelPool;




    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //TODO reconnect use zookeeper !~

        if(cause instanceof IOException){
            ctx.close();
            if(null!=channelPool){
                channelPool.remove(this);
                logger.info("IOException got . remove self from pool .", cause);
            }
        }
        logger.error("Unexpected exception .MayBe server is disconnted.", cause);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(null!=channelPool){
            channelPool.remove(this);
            logger.info("channelInactive,disconnect with {}. remove self from pool .",ctx.channel().remoteAddress());
        }
        super.channelInactive(ctx);
    }
}
