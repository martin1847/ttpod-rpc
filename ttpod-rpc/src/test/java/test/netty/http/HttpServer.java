package test.netty.http;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.search.bean.Pojo;
import com.ttpod.rpc.netty.Server;
import com.ttpod.rpc.netty.telnet.SimpleHttpServerInitializer;
import com.ttpod.rpc.server.AbstractServerProcessor;
import com.ttpod.rpc.server.ServerProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * date: 14-1-28 下午1:11
 *
 * @author: yangyang.cong@ttpod.com
 */
public class HttpServer {
    public static void main(String[] args) {


        Map<String, ServerProcessor> proc = new HashMap<>();
        proc.put("test",new AbstractServerProcessor() {
            public ResponseBean handle(RequestBean req) throws Exception {
                ResponseBean data = new ResponseBean();
                data.setCode(1);
                data.setPages(10);
                data.setRows(2000);
                data.setData(Arrays.asList(new Pojo(req.getData(), 100), new Pojo("OK", 10)));
                return data;
            }
        });

        SimpleHttpServerInitializer ch = new SimpleHttpServerInitializer();
        ch.setProcessors(proc);
        new Server(ch,8080).start();

    }
}
