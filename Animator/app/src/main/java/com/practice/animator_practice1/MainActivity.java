package com.practice.animator_practice1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import static android.R.attr.rotation;

public class MainActivity extends AppCompatActivity {

    ImageView mIv;
    Button mBtn1, mBtn2, mBtn3, mBtn4, mBtn5, mBtn6, mBtn7, mBtn8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIv = (ImageView) findViewById(R.id.iv_duo);
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn2 = (Button) findViewById(R.id.btn_2);
        mBtn3 = (Button) findViewById(R.id.btn_3);
        mBtn4 = (Button) findViewById(R.id.btn_4);
        mBtn5 = (Button) findViewById(R.id.btn_5);
        mBtn6 = (Button) findViewById(R.id.btn_6);
        mBtn7 = (Button) findViewById(R.id.btn_7);
        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Animation loadA= AnimationUtils.loadAnimation(MainActivity.this,R.anim.abc_fade_in);
//                mIv.setTranslationX(100);
//                mIv.setScaleX(scaleX);
//                mIv.setAlpha(alpha);
//                mIv.setRotation(rotation)
//                mIv.setBackgroundColor(color);
//                只要view里面有setXXX()方法就可以通过反射达到变化的目的

                ObjectAnimator oa = ObjectAnimator.ofFloat(mIv, "translationX", 0f, 200f);
//                oa.setRepeatCount(1);//重复次数
//                oa.setStartDelay(1000);//设置延迟执行  动画的间隔时间

//                oa.setRepeatMode(ValueAnimator.RESTART);//重新开始
//                oa.setRepeatMode(ValueAnimator.REVERSE);//反转
                oa.setDuration(500);
                oa.start();
            }
        });

        mBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO -------------多个动画同时执行----------------------
                //方法 1) 设置动画监听，同步操作其他的属性
//                ObjectAnimator animator = ObjectAnimator.ofFloat(mIv, "hehe", 0f, 100f,0f);
                ObjectAnimator animator = ObjectAnimator.ofFloat(mIv, "translationX", 0f, 100f);
                animator.setDuration(500);
//                animator.setRepeatCount(1);//重复次数
//                animator.setRepeatMode(ValueAnimator.REVERSE);//反转
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // 监听动画回调
//				animation.getAnimatedFraction();//动画执行的百分比 0~1 //API 12+
                        float value = (float) animation.getAnimatedValue();//得到0f~100f当中的这个时间点对应的值
                        mIv.setScaleX(0.5f+value/200);
                        mIv.setScaleY(0.5f+value/200);
//                        mIv.setTranslationX(value);
                    }
                });
                animator.start();

//                animator.setRepeatCount(2);//重复次数
//                animator.setRepeatCount(ValueAnimator.INFINITE);//无限重复
//                animator.setRepeatMode(ValueAnimator.REVERSE);//反转
                //重写全部的监听
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}//开始
                    @Override
                    public void onAnimationEnd(Animator animation) {}//结束
                    @Override
                    public void onAnimationCancel(Animator animation) {}//取消
                    @Override
                    public void onAnimationRepeat(Animator animation) {}//重复
                });
//                重写需要的
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {super.onAnimationEnd(animation);}
                });
            }
        });


        mBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 方法 2）---------------ValueAnimator---如果只需要监听值变化就用ValueAnimator---------------
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 100f, 0f);
                animator.setDuration(200);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();//得到0f~100f当中的这个时间点对应的值
                        mIv.setScaleX(0.5f + value / 200);
                        mIv.setScaleY(0.5f + value / 200);
                    }
                });
                animator.start();
            }
        });

        mBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 方法 3) 组合动画
                PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f);
                PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.5f, 1f);
                PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.5f, 1f);
                ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mIv, holder1, holder2, holder3);
                animator.setDuration(1000);
                animator.start();
            }
        });

        mBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 方法 4）-------------动画集合-----------------
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(mIv, "translationX", 0f, 100f, 0f);
//		        animator1.setRepeatCount(3);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(mIv, "alpha", 0f, 1f);
//		        animator2.setStartDelay(startDelay)//设置延迟执行  动画的间隔时间
                ObjectAnimator animator3 = ObjectAnimator.ofFloat(mIv, "scaleX", 0f, 2f, 1f);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(500);
//		        animatorSet.play(animator3).with(animator2).after(animator1);//animator1在前面
                animatorSet.play(animator3).with(animator2).before(animator1);//animator1在后面
//		        animatorSet.playTogether(animator1,animator2,animator3);//一起进行
//                animatorSet.playSequentially(animator1, animator2, animator3);//顺序播放
                animatorSet.start();
            }
        });

        mBtn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 4.------------------案例：实现自由落体抛物线效果---购物车动画、股指数-----------------
                /**
                 * x: 匀速
                 * y: 加速度 y=vt=1/2*g*t*t
                 * 估值器---控制坐标PointF(x,y)
                 */
                ValueAnimator valueAnimator = new ValueAnimator();
//		        valueAnimator.setInterpolator(value)
                valueAnimator.setDuration(2000);
                valueAnimator.setObjectValues(new PointF(0, 0));
//                valueAnimator.setObjectValues(new PointF(0, 0), new PointF(100, 100));
                final PointF pointF = new PointF();
                valueAnimator.setEvaluator(new TypeEvaluator<PointF>() {

                    @Override
                    public PointF evaluate(float fraction, PointF startValue,
                                           PointF endValue) {
                        // 估值计算方法---可以在执行的过程当中干预改变属性的值---做效果：用自己的算法来控制
                        //不断地去计算修改坐标
                        //x匀速运动 x=v*t 为了看起来效果好我让t变成fraction*5
                        pointF.x = 100f * (fraction * 5);
                        //加速度 y=vt=1/2*g*t*t
//			        	pointF.y = 0.5f*9.8f*(fraction*5)*(fraction*5);
                        pointF.y = 5f * 0.5f * 9.8f * (fraction * 5) * (fraction * 5);
                        return pointF;
                    }
                });
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        PointF f = (PointF) animation.getAnimatedValue();
                        mIv.setX(f.x);
                        mIv.setY(f.y);
                    }
                });
                valueAnimator.start();

            }
        });

        mBtn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 6.---------插值器（加速器）Interpolater-----------
                ObjectAnimator oa = ObjectAnimator.ofFloat(mIv, "translationY", 0f, 800f);
                oa.setDuration(800);
//                TimeInterpolator
//                oa.setInterpolator(new AccelerateInterpolator(1));//加速
//                oa.setInterpolator(new AccelerateDecelerateInterpolator());//先加速后减速
//                oa.setInterpolator(new BounceInterpolator());//回弹
//                oa.setInterpolator(new AnticipateInterpolator());//先后撤一下然后正常进行
//                oa.setInterpolator(new CycleInterpolator(0.2f));

                oa.start();
            }
        });

    }
}
