package com.ttpod.rpc.server;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * date: 14-2-17 下午2:49
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class AbstractServerProcessor<ReqType,ResType> implements ServerProcessor<ReqType,ResType> {

    @Override
    public String toString() {
        return description();
    }

    @Override
    public String description() {
        Class clz =  getClass();
        String tmp = clz.getSimpleName();
//        TypeVariable[] typeParameters =  clz.getTypeParameters();

        Type[]  typeParameters = ((ParameterizedType)clz.getGenericSuperclass()).getActualTypeArguments();
        if( typeParameters.length == 0 ){
            typeParameters = ((ParameterizedType)clz.getSuperclass().getGenericSuperclass()).getActualTypeArguments();
        }

        if(typeParameters.length>0){
            tmp += "<" + typeParameters[0].toString().replace("java.lang.","");
        }
        if(typeParameters.length>1){
            tmp += "," + typeParameters[1].toString().replace("java.lang.", "")+">";
        }

        return tmp;
    }
}
