package com.ckming.arouter_api;

import java.util.Map;

/**
 * Created by ptm on 2021/4/21.
 * Group的分组
 */
public interface ARouterGroup {

    /**
     * app、order分组下的信息
     * @return key: app/personal/order  value:分组下的path--class
     */
    Map<String,Class<?extends ARouterPath>> getGroupMap();
}
