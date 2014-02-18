package com.ttpod.rpc.server;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;

/**
 * date: 14-2-18 下午2:52
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class TelnetServerProcessor extends AbstractServerProcessor {

    @Override
    public ResponseBean handle(RequestBean req) throws Exception {
        String data = handle(req.getData());
        ResponseBean res = ResponseBean.code1();
        res.setData(data);
        return res;
    }

    public abstract String handle(String arg) throws Exception;
}
