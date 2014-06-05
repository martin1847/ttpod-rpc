package com.ttpod.rpc.client;

import com.ttpod.rpc.ResponseBean;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * date: 14-2-8 下午10:20
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ResponseObserver<Data> {

    void onSuccess(ResponseBean<Data> response);

    class Blocking<Data> implements ResponseObserver<Data>{
        static final long ONE_MINUTE = 60 * 1000L;
        public volatile ResponseBean<Data> response;
        public void onSuccess(ResponseBean<Data> response) {
            this.response = response;
            synchronized (this) {
                notifyAll();//notify
            }
        }

        public ResponseBean<Data> get(){
            if(null == response) synchronized (this){
                if(null == response)//while (null == response)
                    try {
                        wait(ONE_MINUTE);
                    } catch (InterruptedException ignored) {
                    }
            }
            return response;
        }

    }

    class Future<Data> extends FutureTask<ResponseBean> implements ResponseObserver<Data> {
        private static final Callable<ResponseBean> innerNotUse = new Callable<ResponseBean>() {
            public ResponseBean call() throws Exception {
                return null;
            }
        };
        public Future() {
            super(innerNotUse);
        }
        public void onSuccess(ResponseBean<Data> response) {
            set(response);
        }
    }

}
