package test;

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

/**
 * TODO Comment here.
 * date: 14-2-14 下午7:02
 *
 * @author: yangyang.cong@ttpod.com
 */
public class RealSearch {

    public static void main(String[] args) throws Exception {
        CloseableChannelFactory client = new Client(
                new InetSocketAddress("192.168.8.12", 6666), new DefaultClientInitializer());
        // Read commands from the stdin.
        final ClientHandler handler = client.newChannel().pipeline().get(DefaultClientHandler.class);
        System.out.println("Pls Input a  word ..");
//      final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            String line = in.readLine();
            RequestBean req = new RequestBean((byte) 0, (short) 1, (short) 50, line);
            ResponseBean res = handler.rpc(req);
            System.out.println(line + "  ->  rpc["+ InnerBindUtil.id(req) +"] -> " +res );
            if ("bye".equals(line)) {
                client.shutdown();
                break;
            }
        }

    }
}
