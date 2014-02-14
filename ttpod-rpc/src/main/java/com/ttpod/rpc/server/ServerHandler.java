package com.ttpod.rpc.server;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;


/**
 * date: 14-2-9 下午1:12
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ServerHandler {


    void setProcessors(ServerProcessor[] processors);


    ResponseBean handleRequest(final RequestBean request)throws Exception;

}


