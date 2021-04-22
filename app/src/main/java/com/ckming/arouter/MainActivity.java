package com.ckming.arouter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.arouter_annotation.bean.RouterBean;
import com.ckming.arouter_api.ARouterPath;
import com.ckming.common.utils.Cons;

import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.isRelease) {
            Log.e(Cons.TAG, "当前为：集成化模式，除App可运行外，其他子模块都是Android Lib");
        } else {
            Log.e(Cons.TAG, "当前为：组件化模式，app/order/personal子模块都可独立运行");
        }


    }

    /**
     * 跳转到订单 业务模块
     *
     * @param view
     */
    public void jumpOrder(View view) {

        //以前的跳转方式，但在组件化模式中不行
        /*Intent intent = new Intent(this, Order_MainActivity.class);
        intent.putExtra("name","ckming");
        startActivity(intent);*/


        //注册模块路由地址后，去寻找自动生成的类
        ARouter$$Group$$order group$$order = new ARouter$$Group$$order();
        Map<String, Class<? extends ARouterPath>> groupMap = group$$order.getGroupMap();
        Class<? extends ARouterPath> myClass = groupMap.get("order");

        try {
            ARouter$$Path$$order path = (ARouter$$Path$$order) myClass.newInstance();
            Map<String, RouterBean> pathMap = path.getPathMap();
            RouterBean routerBean = pathMap.get("/order/Order_MainActivity");

            if (routerBean != null) {
                Intent intent = new Intent(this, routerBean.getMyClass());
                Log.e(Cons.TAG, "跳转成功！");
                startActivity(intent);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        //虽然实现了跨模块路由跳转，但还需封装
    }


    /**
     * 跳转到 我的个人中心模块
     *
     * @param view
     */
    public void jumpPersonal(View view) {


    }
}