package com.ali.rpc;

/**
 * date: 15/7/20 11:48
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface SearchStub<Req,Res> {

    Res search(Req request);

}
