package com.ttpod.rpc.server;

import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * date: 14-2-9 下午1:00
 *
 * @author: yangyang.cong@ttpod.com
 */
public interface ServerProcessor<ReqType,ResType> {

    ResponseBean<ResType> handle(RequestBean<ReqType> req) throws Exception;

    String description();

    final class RegCenter {

        public static final RegCenter DEFAULT = new RegCenter();

//        protected RegCenter(){}


        private List<ServerProcessor> processors = new ArrayList<>();
        {
            processors.add(new ServerProcessor() {
                @Override
                public ResponseBean handle(RequestBean req) throws Exception {
                    ResponseBean res = new ResponseBean();
                    List<String> descs = new ArrayList<>(processors.size());
                    for(ServerProcessor sp : processors){
                        descs.add(sp.description());
                    }
                    res.setData(descs);
                    return res;
                }

                @Override
                public String description() {
                    return "Meta Processor,Used to display ALL registered ServerProcessors.";
                }
            });
        }

        public synchronized void regProcessor(ServerProcessor s){
            processors.add(s);
        }

        public synchronized void regProcessor(int index,ServerProcessor s){
            processors.add(index,s);
        }

        public  synchronized void setProcessors(List<ServerProcessor> processors) {
            this.processors.addAll(processors);
        }
        /**
         * after call this, you cann't add more Processor to RegCenter.
         * @return
         */
        public synchronized ServerProcessor[] toArray(){
            ServerProcessor[] array = processors.toArray(new ServerProcessor[processors.size()]);
            processors = Collections.unmodifiableList(processors);
            return array;
        }

    }
}
