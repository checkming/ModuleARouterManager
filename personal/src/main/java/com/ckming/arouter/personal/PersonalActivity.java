package com.ckming.arouter.personal;

import android.os.Bundle;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.personal.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by ptm on 2021/4/22.
 */
@ARouter(path = "/personal/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_activity_main);
    }
}
