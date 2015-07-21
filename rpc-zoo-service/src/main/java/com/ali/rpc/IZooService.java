package com.ali.rpc;

/**
 *
 * Zookeeper Service
 * date: 15/7/16 17:30
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface IZooService<Channel,Stub/* extends SearchStub*/> {



    Stub next();


    boolean available();

    void shutdown();

    void onChannelAdd(String channelConfig);

    void onChannelRemove(String channelConfig, ChannelStub<Channel, Stub> channel);



}
