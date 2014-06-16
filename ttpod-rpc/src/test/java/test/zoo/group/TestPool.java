package test.zoo.group;

import com.ttpod.rpc.netty.pool.impl.CuratorFactory;
import com.ttpod.rpc.netty.pool.impl.ZkChannelPool;
import com.ttpod.rpc.pool.GroupMemberObserver;
import com.ttpod.rpc.netty.pool.impl.DefaultGroupManager;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * date: 14-2-13 下午4:19
 *
 * @author: yangyang.cong@ttpod.com
 */
public class TestPool {

    public static void main(String[] args) throws Exception{
//
//        final ZooKeeper zk = Zoo.connect("192.168.8.12:2181");
//        new DefaultGroupManager(zk,"cyy",new GroupMemberObserver() {
//            public void onChange(List<String> currentNodes) {
//                System.out.println( "onChange : " +  currentNodes);
//            }
//        });


        CuratorFactory cf = new CuratorFactory();
        cf.setUrl("192.168.8.12:2181");
        cf.afterPropertiesSet();

        ZkChannelPool  pool = new ZkChannelPool(cf.getObject(),"solr4/search-songs");

        System.out.println(
                pool.hasNext()
        );

        System.in.read();
    }
}
