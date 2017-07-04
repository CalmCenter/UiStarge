package com.practice.loadanimation;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private FrameLayout mMainView;
    private SplashView splashView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mMainView = new FrameLayout(this);
        ContentView contentView = new ContentView(this);
        mMainView.addView(contentView);
        splashView = new SplashView(this);
        mMainView.addView(splashView);

        setContentView(mMainView);

        //后台开始加载数据
        startLoadData();
    }

    Handler handler = new Handler();

    Thread t=new Thread(new Runnable() {
        @Override
        public void run() {
            //数据加载完毕，进入主界面 --> 开启后面两个动画
            splashView.splashDisappear();
        }
    });

    private void startLoadData() {
        handler.postDelayed(t,5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(t);
    }
}
