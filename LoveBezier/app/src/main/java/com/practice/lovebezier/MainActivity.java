package com.practice.lovebezier;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

	private LoveLayout loveLayout;
	private View first_view;
	private View second_view;
	private Button bt,bt1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loveLayout = (LoveLayout)findViewById(R.id.loveLayout);

		first_view = findViewById(R.id.first);
		second_view = findViewById(R.id.second);

		bt = (Button)findViewById(R.id.bt);
		bt1 = (Button)findViewById(R.id.bt1);

	}
	
	public void start(View v){
		loveLayout.addLoveIcon();
	}
	public void startFirstAnim(View v){
		//显示first_view：1.透明度动画；2.缩放动画；3.翻转动画
		//透明度动画
		ObjectAnimator firstAlphaAnim = ObjectAnimator.ofFloat(first_view, "alpha", 1.0f, 0.7f);
		firstAlphaAnim.setDuration(300);
		//旋转动画1
		ObjectAnimator firstRotationXanim = ObjectAnimator.ofFloat(first_view, "rotationX", 0f,20f,0f);
		firstRotationXanim.setDuration(600);
		//再旋转回来
//		ObjectAnimator firstResumeRotationXanim = ObjectAnimator.ofFloat(first_view, "rotationX", 20f, 0f);
//		firstResumeRotationXanim.setDuration(300);
//		firstResumeRotationXanim.setStartDelay(300);//延迟第一次旋转动画的时间，在这之后再执行
		//缩放动画
		ObjectAnimator firstScaleXAnim = ObjectAnimator.ofFloat(first_view, "ScaleX", 1.0f, 0.8f);
		firstScaleXAnim.setDuration(300);
		ObjectAnimator firstScaleYAnim = ObjectAnimator.ofFloat(first_view, "ScaleY", 1.0f, 0.8f);
		firstScaleYAnim.setDuration(300);
		//由于缩放造成离顶部有一个距离，需要平移
		ObjectAnimator firstTranslationYAnim = ObjectAnimator.ofFloat(first_view, "translationY", 0f, -0.1f*first_view.getHeight());
		firstTranslationYAnim.setDuration(300);
		//第二个view和第一个view动画同时开始执行
		ObjectAnimator secondTranslationYAnim = ObjectAnimator.ofFloat(second_view, "translationY", second_view.getHeight(), 0f);
		secondTranslationYAnim.setDuration(300);
//		secondTranslationYAnim.setStartDelay(200);
		secondTranslationYAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				second_view.setVisibility(View.VISIBLE);
				bt.setClickable(false);
				bt1.setClickable(false);
			}
		});
		AnimatorSet set = new AnimatorSet();
		set.playTogether(
				firstScaleXAnim,
				firstScaleYAnim,
				firstAlphaAnim,
				firstRotationXanim,
				firstTranslationYAnim,
				secondTranslationYAnim
		);
		set.start();

	}
	public void startSecondAnim(View v) {
		//显示first_view:1.透明；2.缩放；3.翻转动画（以什么地方为原点进行翻转？）；4.
		//缩放
		ObjectAnimator firstScaleXAnim = ObjectAnimator.ofFloat(first_view, "scaleX",0.8f, 1.0f);
		firstScaleXAnim.setDuration(300);
		ObjectAnimator firstScaleYAnim = ObjectAnimator.ofFloat(first_view, "scaleY",0.8f, 1.0f);
		firstScaleYAnim.setDuration(300);
		//透明度
		ObjectAnimator firstAlphaAnim = ObjectAnimator.ofFloat(first_view, "alpha",0.5f, 1.0f);
		firstAlphaAnim.setDuration(300);
		//旋转
		ObjectAnimator firstRotationXAnim = ObjectAnimator.ofFloat(first_view, "rotationX", 0f,20f,0f);
		firstRotationXAnim.setDuration(400);
//		//再旋转回来
//		ObjectAnimator firstResumeRotationXAnim = ObjectAnimator.ofFloat(first_view, "rotationX",20f, 0f);
//		firstResumeRotationXAnim.setDuration(200);
//		firstResumeRotationXAnim.setStartDelay(200);//延迟第一次旋转动画的时间，在这之后执行
		//由于缩放造成离顶部有个距离，需要平移上去
		ObjectAnimator firstTranlationYAnim = ObjectAnimator.ofFloat(first_view, "translationY",-0.1f*first_view.getHeight(), 0f);
		firstTranlationYAnim.setDuration(300);

		//第二个view和第一个view动画同时开始执行
		ObjectAnimator secondTranlationYAnim = ObjectAnimator.ofFloat(second_view, "translationY",0f, second_view.getHeight());
		secondTranlationYAnim.setDuration(300);
		secondTranlationYAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				second_view.setVisibility(View.INVISIBLE);
				bt.setClickable(true);
				bt1.setClickable(true);
			}
		});
		AnimatorSet set = new AnimatorSet();
		set.playTogether(
				firstScaleXAnim,
				firstScaleYAnim,
				firstAlphaAnim,
				firstRotationXAnim,
//				firstResumeRotationXAnim,
				firstTranlationYAnim,
				secondTranlationYAnim
		);
		set.start();
	}

}
