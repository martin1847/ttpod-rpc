package com.ttpod.rpc.pool;

import java.util.Iterator;

/**
 * date: 14-2-13 上午11:14
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ChannelPool<ClientHandler> extends Iterator {

//    void add(Channel channel);

    void remove(ClientHandler c);

    void shutdown();

}