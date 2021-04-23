package com.ckming.arouter.order;

import android.os.Bundle;
import android.util.Log;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.arouter_annotation.Parameter;
import com.ckming.arouter_api.ParameterManager;
import com.ckming.common.utils.Cons;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);

        Log.e(Cons.TAG, "我是订单页面");

        //模仿butterknife 进行注册
        ParameterManager.getInstance().loadParameter(this);


        Log.e(Cons.TAG, "从app模块传递过来的参数：name" + name);
    }
}
