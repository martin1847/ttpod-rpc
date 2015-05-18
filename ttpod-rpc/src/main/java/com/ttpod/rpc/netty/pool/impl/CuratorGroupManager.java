package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.pool.GroupManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.curator.x.discovery.ServiceType;
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
                5000, 3000, new RetryUntilElapsed(3600 * 1000, 1500)), groupName);
    }


    private final ConnectionStateListener connectionStateListener = new ConnectionStateListener()
    {
        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState)
        {
            if ( (newState == ConnectionState.RECONNECTED) || (newState == ConnectionState.CONNECTED) )
            {
                try
                {
                    logger.info("Re-registering due to reconnection");
                    reRegisterServices();
                }
                catch ( Exception e )
                {
                    logger.error("Could not re-register instances after reconnection", e);
                }
            }
        }
    };

    private void reRegisterServices() throws Exception
    {
        for ( final Map.Entry<String, byte[]> entry:  ephemeralCache.entrySet() )
        {
            synchronized(entry)
            {
               join(entry.getKey(),entry.getValue());
            }
        }
    }

    public CuratorGroupManager(final CuratorFramework curator,String groupName) {
        this.groupName = Zoo.flipPath(groupName);
        this.curator = curator;
        if (curator.getState() == CuratorFrameworkState.LATENT) {
            curator.start();
        }
        curator.getConnectionStateListenable().addListener(connectionStateListener);
    }


    static final byte[] EMPTY_STR={};
    public String join(String memberName, byte[] data) throws Exception{
        if(null == data){
            data = EMPTY_STR;
        }
        byte[]  oldData = ephemeralCache.putIfAbsent(memberName, data);
        byte[]  useData = (oldData != null) ? oldData : data;
        synchronized (useData) {

            String path = buildPath( memberName );
            final int MAX_TRIES = 2;
            boolean isDone = false;
            for ( int i = 0; !isDone && (i < MAX_TRIES); ++i )
            {
                try
                {
                    logger.info("Create Path {} .",path);
                    CreateMode mode = //(service.getServiceType() == ServiceType.DYNAMIC) ?
                            CreateMode.EPHEMERAL ;
                           // : CreateMode.PERSISTENT;
                    curator.create().creatingParentsIfNeeded().withMode(mode).forPath(path, useData);
                    isDone = true;
                }
                catch ( KeeperException.NodeExistsException e )
                {
                    curator.delete().forPath(path);  // must delete then re-create so that watchers fire
                }
            }
//            try {
//
//                Stat stat = curator.checkExists().forPath(path);
//                if (stat == null) {
//                    return curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
//                            .forPath(path, data);
//                }
//            } catch (KeeperException.NodeExistsException e) {
//                logger.error(" path already exists .", e);
//            } catch (Exception e) {
////            ephemeralCache.remove(memberName);
//                logger.error(memberName + " join group " + groupName + " Faild !!!", e);
//                throw new RuntimeException(memberName + " join group " + groupName + " Faild !!!", e);
//            }
            return path;
        }
    }


    @Override
    public void leave(String memberName) throws Exception {
        try
        {
            curator.delete().guaranteed().forPath(buildPath(memberName));
            ephemeralCache.remove(memberName);
        }
        catch ( KeeperException.NoNodeException ignore )
        {
            // ignore
        }
    }

    @Override
    public String name() {
        return groupName;
    }

    @Override
    public void shutdown() {

        if (null != curator) {
            curator.getConnectionStateListenable().removeListener(connectionStateListener);
            curator.close();
        }
    }


    protected String buildPath(String memberName){
        return groupName + "/" + memberName;
    }
}
