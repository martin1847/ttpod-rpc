/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ttpod.rpc.netty.telnet;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.server.ServerProcessor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handles a server-side channel.
 */
@Sharable
class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(TelnetServerHandler.class);

    final Map<String,ServerProcessor> processors ;
    public TelnetServerHandler(Map<String, ServerProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
//        ctx.write(
//                "Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
//        ctx.write("It is " + new Date() + " now.\r\n");
//        ctx.flush();
    }

    public void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {

        request = request.trim();
        int blank =  request.indexOf(' ');
        String key = request;
        String args = null;
        if(blank>0){
            key = request.substring(0,blank);
            args = request.substring(++blank).trim();
        }
        String response;
        ServerProcessor proc = processors.get(key);
        boolean close = false;
        if(null != proc){
            RequestBean req = new RequestBean();
            req.setData(args);
            response  =String.valueOf(proc.handle(req).getData()) +"\r\n";
        } else if ("bye".equals(request.toLowerCase())) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            response =  usage();
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(response);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Unexpected exception from downstream.", cause);
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        messageReceived(ctx, msg);
    }

    String usage(){
        StringBuilder sb = new StringBuilder(300);
        sb.append("=========Useage=======\n\r");
        for(Map.Entry<String,ServerProcessor> entry : processors.entrySet()){
            sb.append(entry.getKey()).append(" : ").append(entry.getValue().description()).append("\r\n");
        }
        return sb.toString();
    }
}
