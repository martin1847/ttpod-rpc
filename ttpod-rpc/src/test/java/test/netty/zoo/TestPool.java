package test.netty.zoo;

import com.ttpod.rpc.netty.pool.impl.CuratorGroupManager;
import com.ttpod.rpc.netty.pool.impl.ZkChannelPool;
import com.ttpod.rpc.pool.GroupManager;
import org.junit.Test;

/**
 * date: 2014/5/20 17:16
 *
 * @author: yangyang.cong@ttpod.com
 */
public class TestPool {



    String zooUrl = "192.168.8.12:2181";

    @Test
    public void testPool() throws InterruptedException {


        ZkChannelPool pool = new ZkChannelPool(zooUrl,"solr4/qsongs");

        Thread.sleep(1000L);
    }


    @Test
    public void testGroup() throws InterruptedException {


        GroupManager group = new CuratorGroupManager("TEST",zooUrl);
        group.join("CuratorGroupManager","".getBytes());

        Thread.currentThread().join();
    }

}
