package test.netty.query;

import com.ttpod.rpc.netty.Server;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.netty.pool.impl.DefaultGroupManager;
import com.ttpod.rpc.netty.server.DefaultServerHandler;
import com.ttpod.rpc.netty.server.DefaultServerInitializer;
import com.ttpod.rpc.server.AbstractServerProcessor;
import com.ttpod.rpc.server.ServerProcessor;
import com.ttpod.search.bean.Pojo;
import io.netty.util.Version;

import java.util.Arrays;
import java.util.List;

/**
 * date: 14-1-28 下午1:11
 *
 * @author: yangyang.cong@ttpod.com
 */
public class QueryServer {
    public static void main(String[] args) throws Exception{


        System.out.println(
                Version.identify()
        );


        ServerProcessor.RegCenter regCenter = ServerProcessor.RegCenter.DEFAULT;

        regCenter.regProcessor(
            new AbstractServerProcessor<String,List>() {
                public ResponseBean<List> handle(RequestBean<String> req) throws Exception {
                    ResponseBean<List> data = new ResponseBean<>();
                    data.setCode(1);
                    data.setPages(10);
                    data.setRows(2000);
                    data.setData(Arrays.asList(new Pojo(req.getData(), 100), new Pojo("OK", 10)));
                    return data;
                }
            }
        );
        ServerProcessor[]  processors =  regCenter.toArray();
        final DefaultServerHandler serverHandler = new DefaultServerHandler(processors);

        System.out.println(
                processors[0].handle(new RequestBean())
        );

        new Server(new DefaultServerInitializer(serverHandler),6666,new DefaultGroupManager(
              "192.168.8.12:2181","com.ttpod.search"
        )).start();

    }
}
