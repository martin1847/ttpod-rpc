package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.pool.GroupManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date: 2014/5/20 16:00
 *
 * @author: yangyang.cong@ttpod.com
 */
public class CuratorGroupManager implements GroupManager {


    static final Logger logger = LoggerFactory.getLogger(CuratorGroupManager.class);

    String groupName;
    CuratorFramework curator;

    Map<String, byte[]> ephemeralCache = new ConcurrentHashMap<>();


    public CuratorGroupManager(String groupName, String zkAddress) {
        this(groupName, CuratorFrameworkFactory.newClient(zkAddress,
                5000, 3000, new RetryUntilElapsed(3600 * 1000, 1500)));
    }


    public CuratorGroupManager(String groupName,final CuratorFramework curator) {
        this.groupName = Zoo.flipPath(groupName);
        this.curator = curator;
        if (curator.getState() == CuratorFrameworkState.LATENT) {
            curator.start();
        }

        curator.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.RECONNECTED) {
                    for( Map.Entry<String, byte[]> entry:  ephemeralCache.entrySet()){
                        join(entry.getKey(),entry.getValue());
                        logger.info("Rejoin Group : {} Success.",entry.getKey());
                    }
                }
            }
        });
    }


    static final byte[] EMPTY_STR={};
    public String join(String memberName, byte[] data) {
        if(null == data){
            data = EMPTY_STR;
        }
        try {
            ephemeralCache.put(memberName, data);
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(groupName + "/" + memberName, data);
        } catch (Exception e) {
            ephemeralCache.remove(memberName);
            logger.error(memberName + " join group " + groupName + " Faild !!!", e);
            throw new RuntimeException(memberName + " join group " + groupName + " Faild !!!", e);
        }
    }

    @Override
    public String name() {
        return groupName;
    }

    @Override
    public void shutdown() {

        if (null != curator) {
            curator.close();
        }
    }
}
