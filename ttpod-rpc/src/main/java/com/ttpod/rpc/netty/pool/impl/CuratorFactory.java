package com.ttpod.rpc.netty.pool.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 * Maybe used with Spring .
 *
 *<bean id="curatorFactory" class="com.ttpod.rpc.netty.pool.impl.CuratorFactory" p:url="${zookeeper.url}"  />
 *<bean id="curatorFramework" factory-method="getObject" factory-bean="curatorFactory"  />
 *
 * date: 2014/5/21 10:11
 *
 * @author: yangyang.cong@ttpod.com
 */
public class CuratorFactory  /* implements FactoryBean<CuratorFramework>, InitializingBean, DisposableBean */{


    static final Logger log = LoggerFactory.getLogger(CuratorFactory.class);

    int sessionTimeOutMs = 5000;
    int connectionTimeoutMs = 5000;
    int maxElapsedTimeMs = 3600 * 1000;
    int sleepMsBetweenRetries = 1500;

    String url;


    public void setSessionTimeOutMs(int sessionTimeOutMs) {
        this.sessionTimeOutMs = sessionTimeOutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public void setMaxElapsedTimeMs(int maxElapsedTimeMs) {
        this.maxElapsedTimeMs = maxElapsedTimeMs;
    }

    public void setSleepMsBetweenRetries(int sleepMsBetweenRetries) {
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    CuratorFramework curatorFramework;

    @PreDestroy
    public void destroy() throws Exception {
        curatorFramework.close();
    }

    public CuratorFramework getObject() throws Exception {
        return curatorFramework;
    }

    public Class<?> getObjectType() {
        return CuratorFramework.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        curatorFramework = CuratorFrameworkFactory.newClient(url,
                sessionTimeOutMs, connectionTimeoutMs,
                new RetryUntilElapsed(maxElapsedTimeMs, sleepMsBetweenRetries));

        log.info("Start curatorFramework -> {}?sessionTimeOutMs={}&maxElapsedTimeMs={}",
                url,sessionTimeOutMs,maxElapsedTimeMs);

        curatorFramework.start();
    }
}
