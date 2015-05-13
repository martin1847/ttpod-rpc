package com.ttpod.rpc.pool;

/**
 * date: 14-2-13 下午6:37
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface GroupManager {

    String join(String memberName, byte[] data) throws Exception;



    void leave(String memberName) throws Exception;


    String name();


    void shutdown();
}
