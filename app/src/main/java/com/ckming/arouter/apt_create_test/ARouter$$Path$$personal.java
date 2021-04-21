package com.ckming.arouter.apt_create_test;


import com.ckming.arouter_annotation.bean.RouterBean;
import com.ckming.arouter_api.ARouterPath;
import com.ckming.module_arouter.order.Order_MainActivity;

import java.util.HashMap;
import java.util.Map;

// TODO 模板 最终动态生成的效果
// TODO  这就是要用 APT 动态生成的代码
public class ARouter$$Path$$personal implements ARouterPath {

    @Override
    public Map<String, RouterBean> getPathMap() {
        Map<String, RouterBean> pathMap = new HashMap<>();

        pathMap.put("/personal/Personal_MainActivity",
                RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                        Order_MainActivity.class,
                        "/personal/Personal_MainActivity",
                        "personal"));
        return pathMap;
    }
}
