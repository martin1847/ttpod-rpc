package test.netty.query;

import com.ttpod.rpc.InnerBindUtil;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.client.OutstandingContainer;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * date: 14-1-28 下午2:16
 *
 * @author: yangyang.cong@ttpod.com
 */
public class MulitThreadTest {
    public static void main(String[] args) throws Exception {
        CloseableChannelFactory client = new Client(
                new InetSocketAddress("127.0.0.1", 6666), new DefaultClientInitializer());
        // Read commands from the stdin.
        final ClientHandler handler = client.newChannel().pipeline().get(DefaultClientHandler.class);
        final int THREADS = OutstandingContainer.UNSIGN_SHORT_OVER_FLOW;
        ExecutorService exe = Executors.newFixedThreadPool(Math.min(1024,THREADS));
        exe.execute(new Benchmark("assertThreadSafe",handler,exe,THREADS){
            protected void rpcCall(RequestBean<String> req) {
                ResponseBean msg =  handler.rpc(req);
                if (!msg.toString().contains(req.getData())) {
                    System.err.println(req.getData()+ "\t" + InnerBindUtil.id(req) + "\t" + msg);
                }
            }
        });
        Thread.currentThread().join();
        exe.shutdown();

    }
}
