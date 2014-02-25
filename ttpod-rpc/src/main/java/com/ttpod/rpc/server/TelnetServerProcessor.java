package com.ttpod.rpc.server;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * date: 14-2-18 下午2:52
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class TelnetServerProcessor extends AbstractServerProcessor {

    static final Logger logger = LoggerFactory.getLogger(TelnetServerProcessor.class);

    @Override
    public ResponseBean handle(RequestBean req) throws Exception {
        try{
            String data = handle(req.getData());
            ResponseBean res = ResponseBean.code1();
            res.setData(data);
            return res;
        }catch (Throwable e){//For RPC must Notify Client .
            logger.error("handleRequest Error ",e);
            ResponseBean res = ResponseBean.error();
            res.setData(e.getLocalizedMessage());
            return res;
        }
    }

    public abstract String handle(String arg) throws Exception;
}
