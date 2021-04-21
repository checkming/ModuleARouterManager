package com.ckming.module_arouter.order;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.order.R;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);
    }
}
