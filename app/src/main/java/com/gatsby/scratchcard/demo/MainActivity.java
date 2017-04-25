package com.gatsby.scratchcard.demo;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.gatsby.scratchcard.scratchcard.PolymorphView;
import com.gatsby.scratchcard.scratchcard.R;
import com.gatsby.scratchcard.scratchcard.ShadeView;

public class MainActivity extends AppCompatActivity {

    FrameLayout fly;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fly = (FrameLayout) findViewById(R.id.fly);

        final ImageView mInlayer = new ImageView(MainActivity.this);
        mInlayer.setBackgroundResource(R.mipmap.upimage);
        final PolymorphView mPView = new PolymorphView(this);
        mPView.setMeasureInfo(768, 1024);
        mPView.setSurfaceBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.chonglang));
        mPView.setPaintStroke(60);
//        mPView.setUsePercentCounter(false);
//        mPView.setPercentThreshold(20);
//        mPView.setBasisMove(3);
//        mPView.setPadding(30,0,0,0);

        fly.addView(mPView);

        //模拟异步网络请求
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mPView.setInlayer(mInlayer);
            }
        };

        mPView.setGestureListener(new ShadeView.IGesture() {
            @Override
            public void onGestureStart() {
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void onGestureEnd() {
                Toast.makeText(MainActivity.this, "手势结束~", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
