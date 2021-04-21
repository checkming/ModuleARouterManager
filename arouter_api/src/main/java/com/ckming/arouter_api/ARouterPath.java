package com.ckming.arouter_api;

import com.ckming.arouter_annotation.bean.RouterBean;

import java.util.Map;

/**
 * Created by ptm on 2021/4/21.
 *
 * key: /app/MainActivity
 * value：  RouterBean(MainActivity.class)
 *
 */
public interface ARouterPath {
    Map<String, RouterBean> getPathMap();
}
