package com.ttpod.rpc.client;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;

import java.util.concurrent.TimeoutException;

/**
 * date: 14-2-8 下午10:27
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ClientHandler {

    /**
     * async call.
     */
    void rpc(RequestBean req, ResponseObserver observer);

    /**
     * sync call.
     */
    <Data>ResponseBean<Data> rpc(RequestBean req);

    /**
     * sync call.
     */
    <Data>ResponseBean<Data> rpc(RequestBean req,int timeOutMills) throws TimeoutException;

}
