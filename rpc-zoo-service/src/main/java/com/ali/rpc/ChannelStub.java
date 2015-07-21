package com.ali.rpc;

/**
 * date: 15/7/17 16:41
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ChannelStub<Channel,Stub/* extends SearchStub*/> {

    public final Channel channel;

    public Stub stub;

    public ChannelStub(Channel channel) {
        this.channel = channel;
    }
    public ChannelStub(Channel channel,Stub stub) {
        this.channel = channel;
        this.stub = stub;
    }
}
