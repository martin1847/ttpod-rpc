package com.ali.rpc;

import javax.annotation.PostConstruct;

/**
 *
 * Zookeeper Service
 * date: 15/7/16 17:30
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface IZooService<Channel,Stub/* extends SearchStub*/> {


    /**
     * PostConstruct
     */
    @PostConstruct
    void init();

    Stub next();


    boolean available();

    void shutdown();

    void onChannelAdd(String channelConfig);

    void onChannelRemove(String channelConfig, Channel channel);



}
