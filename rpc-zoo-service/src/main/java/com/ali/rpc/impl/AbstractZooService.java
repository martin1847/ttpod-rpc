package com.ali.rpc.impl;

import com.ali.rpc.ChannelStub;
import com.ali.rpc.IZooService;
import com.ali.rpc.SearchStub;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.common.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 15/7/16 18:22
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class AbstractZooService<Channel,Stub/* extends SearchStub*/>
        implements IZooService<Channel,Stub>{


    protected static final Logger logger = LoggerFactory.getLogger(AbstractZooService.class);


    CuratorFramework curator;
    public final String group;

    PathChildrenCache groupMembers;

    public ConcurrentMap<String,ChannelStub<Channel,Stub>> channels = new ConcurrentHashMap<>();
    private volatile ChannelStub<Channel,Stub>[]  stubs;


    protected void refreshChannel(){
        stubs = channels.values().toArray(new ChannelStub[channels.size()]);
    }


    public AbstractZooService(CuratorFramework curator, String group) {
        this.curator = curator;
        this.group = group;

        if( curator.getState() == CuratorFrameworkState.LATENT ) {
            curator.start();
        }
    }

    @PostConstruct
    public void init(){

//        final CountDownLatch waitInit = new CountDownLatch(1);
        if( null == groupMembers )
        try {
            groupMembers = initGroupMembers(curator,group);
            logger.info("wait 2000 ms to initGroupMembers from Zookeeper . ");
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean available() {
        return stubs.length > 0;
    }


    PathChildrenCache initGroupMembers(CuratorFramework curator ,final String groupName){
        PathChildrenCache childrenCache =  new PathChildrenCache(curator,groupName,false);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                ChildData data = event.getData();
                PathChildrenCacheEvent.Type type = event.getType();
                //CONNECTION_LOST CONNECTION_SUSPENDED RECONNECTED
                logger.info("Group {} member {} . ",groupName,type);
                if( null == data){
                    return;
                }
                String ipPort =  ZKPaths.getNodeFromPath(data.getPath());
                if( type == PathChildrenCacheEvent.Type.CHILD_ADDED ){
                    AbstractZooService.this.onChannelAdd(ipPort);
                }else if( type == PathChildrenCacheEvent.Type.CHILD_REMOVED ){
                    ChannelStub<Channel,Stub> cf = channels.remove(ipPort);
                    if(null != cf){
                        logger.info(" CHILD_REMOVED , disconnect from {} ", ipPort);
                    }
                    AbstractZooService.this.onChannelRemove(ipPort, cf);
                }
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




    static final int AVOID_OVER_FLOW = 0xFFFF;
    int tick; // use AtomicInteger ? not needed..
    @Override
    public Stub next() {// allow i to use twice. Although i not thread safe here,but Almost no effect.
        return stubs[ ( ++tick & AVOID_OVER_FLOW )  % stubs.length ].stub;
    }



    @Override
    @PreDestroy
    public void shutdown() {

        logger.info("Shutdown groupMembers ...");
        if(null != groupMembers){
            IOUtils.closeStream(groupMembers);
        }
        logger.info("Shutdown channels ...");
        for(Map.Entry<String,ChannelStub<Channel,Stub>> entry : channels.entrySet()){
            onChannelRemove(entry.getKey(),entry.getValue());
        }
//ss
//        if(null != curator){
//            curator.close();
//        }
    }
}
