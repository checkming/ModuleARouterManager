package com.ckming.arouter.apt_create_test;


import com.ckming.arouter.order.Order_MainActivity;
import com.ckming.arouter_annotation.bean.RouterBean;
import com.ckming.arouter_api.ARouterPath;

import java.util.HashMap;
import java.util.Map;

// TODO 模板 最终动态生成的效果

/**
 * 代表这个路径/order/Order_MainActivity下的所有 --- RouterBean.myClass
 */
public class ARouter$$Path$$order implements ARouterPath {

    /**
     * @return map[key:路径 如: /order/Order_MainActivity, value:RouterBean.myClass]
     */
    @Override
    public Map<String, RouterBean> getPathMap() {
        Map<String, RouterBean> pathMap = new HashMap<>();
        pathMap.put("/order/Order_MainActivity",
                RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                        Order_MainActivity.class,
                        "/order/Order_MainActivity",
                        "order"
                ));
        return pathMap;
    }
}
