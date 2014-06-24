package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;
import com.ttpod.rpc.pool.ChannelPool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 14-6-20 11:56
 *
 * @author: yangyang.cong@ttpod.com
 */
public class NormalPool  implements ChannelPool<ClientHandler> {


    static final Logger logger = LoggerFactory.getLogger(NormalPool.class);


    protected int clientsPerServer;
    protected Map<String,Integer> weightMap = Collections.emptyMap();

    protected ConcurrentMap<String,CloseableChannelFactory> clients = new ConcurrentHashMap<>();
    protected CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();


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
    @PreDestroy
    public void shutdown() {
        closeCachedClients();
    }

    public void setWeightMap(Map<String, Integer> weightMap) {
        this.weightMap = weightMap;
    }
    public void setClientsPerServer(int clientsPerServer) {
        this.clientsPerServer = clientsPerServer;
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, Integer> entry : weightMap.entrySet()) {
            connTo(entry.getKey(),handlers,clients);
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

        CloseableChannelFactory fac = clients.get(ipPort);
        if ( null == fac ) {
            fac = new Client(new InetSocketAddress(ip, port), USE_NIO, new DefaultClientInitializer());
            clients.put(ipPort, fac);
        }else{
            logger.info("reuse already exists client for : {} ",ipPort);
        }
        int clientWeight = clientsPerServer;
        if(!weightMap.isEmpty()){
            Integer myWight = weightMap.get(ipPort);
            if(null != myWight && myWight > 0){
                int total = 0;
                for(int w : weightMap.values()){total+=w;}
                if(total>0){
                    clientWeight = Math.max(1,Math.round(Runtime.getRuntime().availableProcessors() * myWight/(float)total));
                    logger.info(" {} weight of clients {} " ,ipPort,clientWeight);
                }
            }
        }
        // zooKeeper.getData(groupName+"/"+addr,false, null);
        List<ClientHandler> tmp = new ArrayList<>(clientWeight);
        for(int i = clientWeight;i>0;i--){
            tmp.add(fetchHandler(fac.newChannel()));
        }
        handlers.addAll(tmp);
        Collections.shuffle(handlers);
        logger.info("Success Connect To  : {} ,Establish {} Connections" , ipPort , clientWeight);
    }


    public void remove() {
        throw new UnsupportedOperationException();
    }

}
