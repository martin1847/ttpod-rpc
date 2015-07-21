package com.ali.rpc.impl;

import com.ali.rpc.IZooService;
import com.ali.rpc.SearchStub;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * date: 15/7/20 12:07
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class AbstractParallelClient<Req,Res>  implements SearchStub<Req,Res>{


    static final ExecutorService EXE = new ThreadPoolExecutor(64, 256, 60L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(512));

    protected static final Logger logger = LoggerFactory.getLogger(AbstractParallelClient.class);
    protected Res EMPTY = null;

//    ExecutorCompletionService

    CuratorFramework curator;
    String groupBase;

    protected volatile Set<IZooService<Object,SearchStub<Req,ListenableFuture<Res>>>> availableServices =
            new CopyOnWriteArraySet<IZooService<Object,SearchStub<Req,ListenableFuture<Res>>>>();



    public void setCurator(CuratorFramework curator) {
        this.curator = curator;
    }

    public void setGroupBase(String groupBase) {
        this.groupBase = groupBase;
    }

    @PostConstruct
    /**
     * 不考虑动态增加group情况，一次初始化完成，减少复杂度
     */
    public void  init(){

        if( curator.getState() == CuratorFrameworkState.LATENT ) {
            curator.start();
        }
        try {
            for(String child :  curator.getChildren().forPath(groupBase)){
                initService(curator, groupBase + '/' + child);
            }
            logger.info("init ParallelClient .");

        } catch (Exception e) {
            logger.error("init ParallelClient error ",e);
            e.printStackTrace();
        }
    }


    @PreDestroy
    public void shutdown(){
        for(IZooService s : availableServices){
            s.shutdown();
        }
        logger.info("close  ParallelClient .");
        EXE.shutdown();
    }


    public Res search(Req request) {
        Set<IZooService<Object,SearchStub<Req,ListenableFuture<Res>>>> availableServices = this.availableServices;
        int n = availableServices.size();
        final BlockingQueue<Future<Res>> completionQueue = new LinkedBlockingQueue<Future<Res>>(n);
        List<Future<Res>> futures = new ArrayList<Future<Res>>(n);

        for(final IZooService<Object,SearchStub<Req,ListenableFuture<Res>>> zs : availableServices ){
            final ListenableFuture<Res> resListenableFuture = zs.next().search(request);
            resListenableFuture.addListener(new AddToQueen<Res>(completionQueue,resListenableFuture), EXE);
            futures.add(resListenableFuture);
        }

        try {
            for (int i = 0; i < n; ++i) {
                Res r = completionQueue.take().get();
                if (canUse(r)) {
                    return r;
                }
            }
        }catch (Exception ignore) {
            ignore.printStackTrace();
        }finally {
            for(Future<Res> f :futures ){
                f.cancel(true);
            }
        }

        return EMPTY;
    }


    static class AddToQueen<V> implements Runnable{
        final BlockingQueue<Future<V>> queue;
        final Future<V> future;

        public AddToQueen(BlockingQueue<Future<V>> queue, Future<V> future) {
            this.queue = queue;
            this.future = future;
        }

        @Override
        public void run() {
            queue.add(future);
        }
    }

    protected abstract boolean canUse(Res r);


    protected abstract void initService(CuratorFramework curator,String group);
}
