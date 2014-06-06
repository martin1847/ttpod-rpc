package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.pool.GroupManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
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


    public CuratorGroupManager(String zkAddress, String groupName) {
        this(CuratorFrameworkFactory.newClient(zkAddress,
                5000, 3000, new RetryUntilElapsed(3600 * 1000, 1500)),groupName);
    }


    public CuratorGroupManager(final CuratorFramework curator,String groupName) {
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
        ephemeralCache.put(memberName, data);
        String path = groupName + "/" + memberName;
        try {

            Stat stat = curator.checkExists().forPath(path);
            if(stat == null){
                return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(path, data);
            }
        }catch (KeeperException.NodeExistsException e){
            logger.error(" path already exists .",e);
        }catch (Exception e) {
//            ephemeralCache.remove(memberName);
            logger.error(memberName + " join group " + groupName + " Faild !!!", e);
            throw new RuntimeException(memberName + " join group " + groupName + " Faild !!!", e);
        }
        return path;
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
