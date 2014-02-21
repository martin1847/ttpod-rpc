package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.pool.ChannelPool;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;
import com.ttpod.rpc.pool.GroupManager;
import com.ttpod.rpc.pool.GroupMemberObserver;
import io.netty.channel.Channel;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 14-2-13 下午2:21
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ZkChannelPool implements ChannelPool<ClientHandler> {

    static final Logger logger = LoggerFactory.getLogger(ZkChannelPool.class);


    CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();

    String zkAddress ;
    String groupName;
    int clientsPerServer;


    Map<String,CloseableChannelFactory> connPool = new ConcurrentHashMap<>();

    public ZkChannelPool(String zkAddress,String groupName){
        this(zkAddress,groupName,2);
    }

    public ZkChannelPool(String zkAddress,String groupName, int clientsPerServer) {
        this.zkAddress = zkAddress;
        this.clientsPerServer = clientsPerServer;
        this.groupName = groupName;

        init();
    }

    ZooKeeper zooKeeper;
    GroupManager groupManager;

    int tick;
    public void init(){
        zooKeeper = Zoo.connect(zkAddress);
        groupManager = new DefaultGroupManager(zooKeeper,groupName,new GroupMemberObserver() {
            public void onChange(List<String> currentNodes) {
                setUpClient(currentNodes);
            }
        });
        resetTickPeriod();
    }

    void resetTickPeriod(){
        new Thread(new Runnable() {
            static final long ONE_HOUR = 3600 * 1000L;
            public void run() {
                while (true){
                    try {
                        Thread.sleep(ONE_HOUR);
                    } catch (InterruptedException ignored) {
                    }
                    tick = 0;
                }
            }
        },"ZkChannelPool.resetTickPeriod").start();
    }


    @Override
    public ClientHandler next() {// allow i to use twice.
        return handlers.get( tick++ % handlers.size() );
    }

    @Override
    public void remove(ClientHandler c) {
        handlers.remove(c);
    }

    @Override
    public void shutdown() {
        for( Map.Entry<String,CloseableChannelFactory> entry: connPool.entrySet()){
            logger.info("Disconnect From : {}", entry.getKey());
            entry.getValue().shutdown();
        }
        logger.info("Shutdown GroupManager ...");
        groupManager.shutdown();
    }

    ClientHandler fetchHandler(Channel channel){
        DefaultClientHandler handler = channel.pipeline().get(DefaultClientHandler.class);
        handler.setChannelPool(this);
        return handler;
    }


    void setUpClient(List<String> ipPorts){

        Set<String> clsoed =  connPool.keySet();
        clsoed.removeAll(ipPorts);

        for(String needClose : clsoed){
            connPool.remove(needClose).shutdown();
        }

        ipPorts.removeAll(clsoed);

        for (String addr : ipPorts){
            String[] ip_port = addr.trim().split(":");
            String ip = ip_port[0];
            int port = Integer.parseInt(ip_port[1]);

            //TODO server weight
//            zooKeeper.getData(groupName+"/"+addr,false, null);

            try {
                CloseableChannelFactory fac = new Client(new InetSocketAddress(ip,port),new DefaultClientInitializer());
                connPool.put(addr,fac);
                logger.info("Success Connect To  : {}" , addr);
                for(int i = clientsPerServer;i>0;i--){
                    handlers.add(fetchHandler(fac.newChannel()));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


}
