package com.ckming.arouter.order;

import android.os.Bundle;
import android.util.Log;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.common.utils.Cons;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);

        Log.e(Cons.TAG,"我是订单页面");
    }
}
