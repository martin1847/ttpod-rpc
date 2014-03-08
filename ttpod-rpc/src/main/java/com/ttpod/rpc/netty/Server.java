package com.ttpod.rpc.netty;

import com.ttpod.rpc.pool.GroupManager;
import com.ttpod.rpc.util.IpAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * date: 14-1-7 下午4:20
 *
 * @author: yangyang.cong@ttpod.com
 */
public class Server {


    static final boolean LINUX = System.getProperty("os.name").toLowerCase(Locale.UK).trim().startsWith("linux");

    static final Logger logger = LoggerFactory.getLogger(Server.class);

    int port;

    //http://netty.io/news/2014/02/25/4-0-17-Final.html
    EventLoopGroup bossGroup = new NioEventLoopGroup();//LINUX ? new EpollEventLoopGroup() :  new NioEventLoopGroup(); // (1)
    EventLoopGroup workerGroup = new NioEventLoopGroup(); // TODO USE  LocalEventLoop ?
    Channel channel;
    ChannelHandler channelHandler;
    GroupManager groupManager;

    public Server(){}

    public Server(ChannelHandler channelHandler) {
        this(channelHandler,8080,null);
    }
    public Server(ChannelHandler channelHandler, int port) {
        this(channelHandler,port,null);
    }

    public Server(ChannelHandler channelHandler, int port,GroupManager groupManager) {
        this.channelHandler = channelHandler;
        this.port = port;
        this.groupManager = groupManager;
    }


    public void start(){


        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)use NioServerSocketChannel instantiate a new Channel to accept incoming connections.
                    .option(ChannelOption.SO_BACKLOG, 128) //  option() is for the NioServerSocketChannel that accepts incoming connections
                    .childHandler(channelHandler)//(4)
                    .childOption(ChannelOption.TCP_NODELAY, true)  // (5)childOption() is for the Channels accepted by the parent ServerChannel
                    .childOption(ChannelOption.SO_KEEPALIVE, true);// which is NioServerSocketChannel in this case.

            // Bind and start to accept incoming connections.
            channel = b.bind(port).sync().channel(); // (7)

            System.out.println("Starting server at "+ IpAddress.eth0IpOrHostName() +":"+port);

            if(groupManager != null){
                groupManager.join(IpAddress.eth0IpOrHostName() +":"+port,null);
                System.out.println("server joined : "+ groupManager.name());
            }

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void shutdown(){

        if(null != groupManager){
            groupManager.shutdown();
            logger.info("wait 2s to exit group,so clients not use this server.");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(null!= channel){
            channel.close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }



    public void setChannelHandler(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public void setPort(int port) {
        this.port = port;
    }



}
