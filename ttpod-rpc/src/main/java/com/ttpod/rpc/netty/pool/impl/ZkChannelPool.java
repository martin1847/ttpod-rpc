package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;
import com.ttpod.rpc.pool.ChannelPool;
import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.common.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 14-2-13 下午2:21
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ZkChannelPool extends NormalPool {

    static final Logger logger = LoggerFactory.getLogger(ZkChannelPool.class);

    static final String WEIGHT_PRE = "/Config/ServerWeight";



    String groupName;
    String weightPath;
//    int clientsPerServer;
    CuratorFramework curator;
    PathChildrenCache groupMembers;
    NodeCache weightPropertiesCache;
//    Map<String,Integer> weightMap = Collections.emptyMap();
//
//    ConcurrentMap<String,CloseableChannelFactory> clients = new ConcurrentHashMap<>();
//    CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();

    public ZkChannelPool(String zkAddress,String groupName){
        this(zkAddress,groupName,2);
    }
    public ZkChannelPool(CuratorFramework curator , final String groupNameNormal){
        this(curator,groupNameNormal,2);
    }

//    @Deprecated
    public ZkChannelPool(String zkAddress,String groupName, int clientsPerServer) {
        this(CuratorFrameworkFactory.newClient(zkAddress,
                5000, 3000, new RetryNTimes(10, 1500)),groupName,clientsPerServer);
    }
//    @Deprecated
    public ZkChannelPool(CuratorFramework curator , final String groupNameNormal, int clientsPerServer) {
        this.curator = curator;
        this.clientsPerServer = clientsPerServer;

        this.groupName = Zoo.flipPath(groupNameNormal);
        this.weightPath = WEIGHT_PRE + this.groupName;

        if( curator.getState() == CuratorFrameworkState.LATENT ) {
            curator.start();
        }

        weightPropertiesCache = initWeight(curator, weightPath);
        groupMembers = initGroupMembers(curator,groupName);


    }


    void buildWeightMap(NodeCache cache) throws IOException {
        ChildData data =  cache.getCurrentData();
        byte[] bytes;
        if( null != data  && null != (bytes = data.getData()) ){
            Properties  prop =  new Properties();
            prop.load(new StringReader(new String(bytes, CharsetUtil.UTF_8)));
            Map<String,Integer> wMap = new HashMap<>();
            for(String key : prop.stringPropertyNames()){
                wMap.put( key, Integer.valueOf(prop.getProperty(key).trim()) );
            }
            weightMap = wMap;
            logger.info("refresh service weight {} .",prop);
        }
    }

    NodeCache initWeight(CuratorFramework curator ,String weightPath){
        final NodeCache cache= new NodeCache(curator, weightPath);
        cache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                buildWeightMap(cache);
            }
        });
        try {
            cache.start(true);
            buildWeightMap(cache);
        } catch (Exception e) {
            logger.error("Start NodeCache error for path: {}, error info: {}",  weightPath, e.getMessage());
        }
        return cache;
    }

    PathChildrenCache initGroupMembers(CuratorFramework curator ,final String groupName){
        PathChildrenCache childrenCache =  new PathChildrenCache(curator,groupName,false);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                ChildData data = event.getData();
                PathChildrenCacheEvent.Type type = event.getType();
                //CONNECTION_LOST CONNECTION_SUSPENDED RECONNECTED
                logger.info("Group {} member {}  -> {} ",groupName,type,data);
                if( null == data){
                    return;
                }
                String ipPort =  ZKPaths.getNodeFromPath(data.getPath());
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
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            logger.error("Error Start Connect To Members of Group " + groupName,e);
            e.printStackTrace();
        }
        return childrenCache;
    }


    @Override
    public void shutdown() {
        closeCachedClients();
        logger.info("Shutdown CuratorFramework ...");


        if(null != weightPropertiesCache){
            IOUtils.closeStream(weightPropertiesCache);
        }

        if(null != groupMembers){
            IOUtils.closeStream(groupMembers);
        }

        if(null != curator){
            curator.close();
        }
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




}
