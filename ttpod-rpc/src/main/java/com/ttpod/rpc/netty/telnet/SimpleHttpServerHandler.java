package com.ttpod.rpc.netty.telnet;

import com.alibaba.fastjson.JSON;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.server.ServerProcessor;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * date: 14-2-27 下午6:39
 *
 * @author: yangyang.cong@ttpod.com
 */
class SimpleHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final Logger logger = LoggerFactory.getLogger(SimpleHttpServerHandler.class);

    final Map<String, ServerProcessor> processors;

    public SimpleHttpServerHandler(Map<String, ServerProcessor> processors) {
        this.processors = processors;
    }
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String json;
        QueryStringDecoder query = new QueryStringDecoder(request.getUri());
//        if (is100ContinueExpected(request)) {
//            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
//            ctx.write(response);
//        }

        String path = query.path().substring(1);
        ServerProcessor proc = processors.get(path);
        if (null != proc) {
            RequestBean req = new RequestBean();
            try{
                Map<String, List<String>> param = query.parameters();
                req.setData(trim(param, "data"));
                String size = trim(param, "size");
                if (size.length() > 0) {
                    req.setSize(Integer.parseInt(size));
                }
                String page = trim(param, "page");
                if (page.length() > 0) {
                    req.setPage(Integer.parseInt(page));
                }
                ResponseBean responseBean = proc.handle(req);
                json = JSON.toJSONString(responseBean);
            }catch (Exception e){
                e.printStackTrace();
                json = e.toString();
            }
        }else{
            json = TelnetServerHandler.usage(processors);
        }

        write(json,ctx,request);
    }

    String trim(Map<String, List<String>> param, String key) {
        List<String> v = param.get(key);
        if (null == v || v.isEmpty()) {
            return "";
        }
        return v.get(0).trim();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    void write(String json,ChannelHandlerContext ctx,FullHttpRequest request){
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/json; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        boolean keepAlive = isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add 'Content-Length' header only for a keep-alive connection.
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);
        // Write the end marker
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        // Decide whether to shutdown the connection or not.
        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }
}
