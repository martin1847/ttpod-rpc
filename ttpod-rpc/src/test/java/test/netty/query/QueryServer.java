package test.netty.query;

import com.ttpod.rpc.netty.Pojo;
import com.ttpod.rpc.netty.Server;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.netty.pool.impl.DefaultGroupManager;
import com.ttpod.rpc.netty.server.DefaultServerHandler;
import com.ttpod.rpc.netty.server.DefaultServerInitializer;
import com.ttpod.rpc.server.AbstractServerProcessor;
import com.ttpod.rpc.server.ServerProcessor;
import io.netty.util.Version;

import java.util.Arrays;

/**
 * date: 14-1-28 下午1:11
 *
 * @author: yangyang.cong@ttpod.com
 */
public class QueryServer {
    public static void main(String[] args) {


        System.out.println(
                Version.identify()
        );

        final DefaultServerHandler serverHandler = new DefaultServerHandler();
        ServerProcessor.RegCenter regCenter = ServerProcessor.RegCenter.DEFAULT;

        regCenter.regProcessor(
            new AbstractServerProcessor() {
                public ResponseBean handle(RequestBean req) throws Exception {
                    ResponseBean data = new ResponseBean();
                    data.setCode(1);
                    data.setPages(10);
                    data.setRows(2000);
                    data.setData(Arrays.asList(new Pojo(req.getData(), 100), new Pojo("OK", 10)));
                    return data;
                }
            }
        );
        new Server(new DefaultServerInitializer(serverHandler),6666,new DefaultGroupManager(
              "192.168.8.12:2181","com.ttpod.search"
        ));

    }
}
