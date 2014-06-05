package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;
import com.ttpod.rpc.pool.ChannelPool;
import io.netty.channel.Channel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.common.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 14-2-13 下午2:21
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ZkChannelPool implements ChannelPool<ClientHandler> {

    static final Logger logger = LoggerFactory.getLogger(ZkChannelPool.class);




    String groupName;
    int clientsPerServer;
    CuratorFramework curator;
    PathChildrenCache groupMembers;
    CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();
    private ConcurrentMap<String,CloseableChannelFactory> clients = new ConcurrentHashMap<>();

    public ZkChannelPool(String zkAddress,String groupName){
        this(zkAddress,groupName,
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
        );
    }

    public ZkChannelPool(String zkAddress,String groupName, int clientsPerServer) {
        this(CuratorFrameworkFactory.newClient(zkAddress,
                5000, 3000, new RetryNTimes(10, 1500)),groupName,clientsPerServer);
    }

    public ZkChannelPool(CuratorFramework curator , final String groupNameNormal, int clientsPerServer) {
        this.curator = curator;
        this.clientsPerServer = clientsPerServer;
        this.groupName = Zoo.flipPath(groupNameNormal);

        if( curator.getState() == CuratorFrameworkState.LATENT ) {
            curator.start();
        }

        groupMembers = new PathChildrenCache(curator,groupName,false);
        groupMembers.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                PathChildrenCacheEvent.Type type = event.getType();
                if( type == PathChildrenCacheEvent.Type.INITIALIZED){
                    return;
                }
                String ipPort =  ZKPaths.getNodeFromPath(event.getData().getPath());
                logger.info("Group {} member {}  -> {} ",groupName,type,ipPort);
                if( type == PathChildrenCacheEvent.Type.CHILD_ADDED ){
                    connTo(ipPort,handlers,clients);
                }else if( type == PathChildrenCacheEvent.Type.CHILD_REMOVED ){
                    CloseableChannelFactory cf = clients.remove(ipPort);
                    if(null != cf){
                        logger.info(" CHILD_REMOVED , disconnect from {} ",ipPort);
                        cf.shutdown();
                    }
                }
//                List<String> ipPorts = new ArrayList<>();
//                for(ChildData data : groupMembers.getCurrentData()){
//                    ipPorts.add();
//                }
//
//                resetUpClient(ipPorts);
            }
        });
        try {
            groupMembers.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            logger.error("Error Start Connect To Members of Group " + groupName,e);
            e.printStackTrace();
        }


    }

    static final int AVOID_OVER_FLOW = 0xFFFF;
    int tick; // use AtomicInteger ? not needed..
    @Override
    public ClientHandler next() {// allow i to use twice. Although i not thread safe here,but Almost no effect.
        return handlers.get(  ( ++tick & AVOID_OVER_FLOW )  % handlers.size() );
    }



    @Override
    public boolean hasNext() {
        return handlers.size() != 0;
    }

    @Override
    public void remove(ClientHandler c) {
        handlers.remove(c);
    }

    @Override
    public void shutdown() {
        closeCachedClients();
        logger.info("Shutdown CuratorFramework ...");

        if(null != groupMembers){
            IOUtils.closeStream(groupMembers);
        }

        if(null != curator){
            curator.close();
        }
    }

    void closeCachedClients(){
        for( Map.Entry<String,CloseableChannelFactory> entry: clients.entrySet()){
            logger.info("Disconnect From : {}", entry.getKey());
            entry.getValue().shutdown();
        }
    }
    ClientHandler fetchHandler(Channel channel){
        DefaultClientHandler handler = channel.pipeline().get(DefaultClientHandler.class);
        handler.setChannelPool(this);
        return handler;
    }


    static final boolean USE_NIO = ! Boolean.getBoolean("Client.BIO");



    synchronized void connTo(String ipPort,List<ClientHandler> handlers,Map<String,CloseableChannelFactory> clients){
        String[] ip_port = ipPort.trim().split(":");
        String ip = ip_port[0];
        int port = Integer.parseInt(ip_port[1]);

        CloseableChannelFactory fac = new Client(new InetSocketAddress(ip,port),USE_NIO,new DefaultClientInitializer());
        clients.put(ipPort,fac);
        //TODO server weight
        // zooKeeper.getData(groupName+"/"+addr,false, null);
        for(int i = clientsPerServer;i>0;i--){
            handlers.add(fetchHandler(fac.newChannel()));
        }
        logger.info("Success Connect To  : {} ,Establish {} Connections" , ipPort , clientsPerServer);
    }

    @Deprecated
    synchronized void  resetUpClient(List<String> ipPorts){

        CopyOnWriteArrayList<ClientHandler> newHandlers = new CopyOnWriteArrayList<>();
        ConcurrentMap<String,CloseableChannelFactory> newClients = new ConcurrentHashMap<>();
        for (String addr : ipPorts){
            connTo(addr,newHandlers,newClients);
        }
        Collections.shuffle(newHandlers);
        this.handlers = newHandlers;//
        logger.info(" close old connected clients {} ",this.clients.entrySet());
        closeCachedClients();
        this.clients = newClients;
    }


    public void remove() {
        throw new UnsupportedOperationException();
    }

}
