package com.ckming.arouter_api;

/**
 * Created by ptm on 2021/4/23.
 */
public interface ParameterGet {

    /**
     * 目标对象.属性名 = getIntent().属性类型。  ==>完成赋值操作
     * @param targetParameter  目标对象中的那些属性
     */
    void getParameter(Object targetParameter);
}
