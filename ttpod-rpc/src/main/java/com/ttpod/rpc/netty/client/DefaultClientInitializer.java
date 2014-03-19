package com.ttpod.rpc.netty.client;

import com.ttpod.rpc.netty.codec.StringReqEncoder;
import com.ttpod.rpc.netty.codec.ResponseDecoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultClientInitializer
 *
 * date: 14-2-12 下午4:24
 *
 * @author: yangyang.cong@ttpod.com
 */
public class DefaultClientInitializer extends ChannelInitializer<SocketChannel> {


    static final Logger logger = LoggerFactory.getLogger(DefaultClientInitializer.class);

    final ChannelHandler requestEncoder = new StringReqEncoder();
    final ChannelHandler responseDecoder = new ResponseDecoder();

    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
//        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
        p.addLast("responseDecoder", responseDecoder);
        p.addLast("requestEncoder", requestEncoder);
        initClientHandler(p);
    }


//    static final int Threads = Runtime.getRuntime().availableProcessors()
//            * Integer.getInteger("ClientInitializer.cpu_multiple",0);

    static final boolean separatePool = Boolean.getBoolean("ClientInitializer.separatePool");
    protected void  initClientHandler(ChannelPipeline p){
        // TODO use separate thread threads ? new LocalEventLoopGroup(),
        // but just because too slow the server is.
        // http://www.infoq.com/cn/articles/java-threadPool
        // 依赖数据库连接池的任务，因为线程提交SQL后需要等待数据库返回结果，如果等待的时间越长CPU空闲时间就越长，那么线程数应该设置越大，这样才能更好的利用CPU。
        // 建议使用有界队列，有界队列能增加系统的稳定性和预警能力，可以根据需要设大一点，比如几千。
//        p.addLast(new LocalEventLoopGroup(Threads), new DefaultClientHandler());
        EventExecutorGroup group = separatePool ? new LocalEventLoopGroup() : null;
        if(separatePool){
            logger.info(" USE separate  [ LocalEventLoopGroup ] for DefaultClientHandler .");
        }
        p.addLast(group,new DefaultClientHandler());
    }
}
