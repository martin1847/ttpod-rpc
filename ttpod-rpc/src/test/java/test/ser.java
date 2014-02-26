package test;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.ttpod.rest.common.util.JSONUtil;
import com.ttpod.rpc.ResponseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * date: 14-2-26 下午1:52
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ser {

    static final Schema<ResponseBean> schema =  RuntimeSchema.getSchema(ResponseBean.class);

    public static void main(String[] args) {
        String json = "   {\"id\":\"19302\",\"desc\":\"暂无简介\",\"song_ids\":\"5884218\",\"index_field\":\"tha eimai ego edo|ok|tha eimai ego edook|oktha eimai ego edo\",\"publish_time\":\"2012-03-09\",\"song_total\":\"1\",\"name\":\"Tha Eimai Ego Edo\",\"singer_name\":\"OK\",\"lang\":\"英语\"}\n" +
                "   ";



        ResponseBean useMap = initBean(json,HashMap.class,10);
        ResponseBean useBean = initBean(json, Bean.class, 10);

        byte[] beanArray =   toByte(useBean);
        byte[] mapArray =   toByte(useMap);
        System.out.println(
                "beanData length : " + toByte(useBean).length +" , mapData length : " + toByte(useMap).length
        );
        for(int i = 0; i< 5000 ; i++ ){//hot jvm
            toByte(useBean);
            toByte(useMap);
            toResponse(beanArray);
            toResponse(mapArray);
        }

        int test = 100000;
        long l = System.currentTimeMillis();
        for(int i = 0;i<test;i++){
            toByte(useBean);
        }
        long t = System.currentTimeMillis() -l;
        System.out.println("bean encode cost : " + t +" ms, avg : " + t/test);

        l = System.currentTimeMillis();
        for(int i = 0;i<test;i++){
            toByte(useMap);
        }
        t = System.currentTimeMillis() -l;
        System.out.println("map encode cost : " + t +" ms, avg : " + t/test);


        l = System.currentTimeMillis();
        for(int i = 0;i<test;i++){
            toResponse(beanArray);
        }
        t = System.currentTimeMillis() -l;
        System.out.println("bean decode cost : " + t +" ms, avg : " + t/test);


        l = System.currentTimeMillis();
        for(int i = 0;i<test;i++){
            toResponse(mapArray);
        }
        t = System.currentTimeMillis() -l;
        System.out.println("map decode cost : " + t +" ms, avg : " + t/test);



    }


    static byte[] toByte(ResponseBean res){
        return ProtostuffIOUtil.toByteArray(res, schema, LinkedBuffer.allocate(1024));
    }

    static ResponseBean toResponse( byte[] res){
        ResponseBean pojo = new ResponseBean();
        ProtostuffIOUtil.mergeFrom(res, pojo, schema);
        return pojo;
    }



    static ResponseBean initBean(String json,Class clz, int reply){
        ResponseBean bean = ResponseBean.code1();
        List data = new ArrayList(reply);
        bean.setData(data);

        for(int i = 0 ;i < reply; i++){
            data.add(JSONUtil.jsonToBean(json,clz));
        }

        return bean;
    }

}


