package com.ckming.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * Created by ptm on 2021/4/23.
 * 参数的加载管理器：用于接收参数
 */
public class ParameterManager {

    public static ParameterManager instance;

    public static ParameterManager getInstance(){
        if (instance == null){
            synchronized (ParameterManager.class){
                if (instance == null){
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    //LRU缓存<类名,参数加载接口>
    private LruCache<String,ParameterGet> cache;

    private ParameterManager(){
        cache = new LruCache<>(100);
    }

    //eg:Order_MainActivity + $$Parameter
    static final String FILE_SUFFIX_NAME = "$$Parameter";

    public void loadParameter(Activity activity){
        String className = activity.getClass().getName();

        ParameterGet parameterLoad = cache.get(className);
        if (null == parameterLoad){
            try {
                //Order_MainActivity + $$Parameter
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);

                //接口实现类的class
                parameterLoad = (ParameterGet) aClass.newInstance();
                cache.put(className,parameterLoad);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterLoad.getParameter(activity);  //执行之前生成的类
    }
}
