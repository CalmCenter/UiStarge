package com.beyond.contentmenu;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;


public class BouncingMenu {
	private ViewGroup mParentVG;
	private View rootView;
	private BouncingView bouncingView;
	private RecyclerView recyclerView;

	public BouncingMenu(View view, int resId, final MyRecyclerAdapter adapter) {
		//1.找到帧布局
		mParentVG = findRootParent(view);
		//2.渲染菜单布局
		rootView = LayoutInflater.from(view.getContext()).inflate(resId, null, false);
		bouncingView = (BouncingView)rootView.findViewById(R.id.sv);
		recyclerView = (RecyclerView)rootView.findViewById(R.id.rv);
		recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		bouncingView.setMyAnimationListener(new BouncingView.MyAnimationListener() {
			
			@Override
			public void showContent(){
				recyclerView.setVisibility(View.VISIBLE);
				recyclerView.setAdapter(adapter);
				recyclerView.scheduleLayoutAnimation();
			}
		});
	}
	
	private ViewGroup findRootParent(View view) {
//		((Activity)view.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
		do{
			if(view instanceof FrameLayout){
				if(view.getId()==android.R.id.content){//android:id="@android:id/content"
					return (ViewGroup) view;
				}
			}
			if(view!=null){
				ViewParent parent = view.getParent();
				view = parent instanceof View ?(View)parent:null;
			}
		}while(view!=null);
		return null;
	}

	public static BouncingMenu makeMenu(View view, int resId, MyRecyclerAdapter adapter){
		return new BouncingMenu(view, resId,adapter);
	}
	
	public BouncingMenu show(){
		//3.将菜单布局add到帧布局
		if(rootView.getParent()!=null){
			mParentVG.removeView(rootView);
		}
		LayoutParams lp =  new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mParentVG.addView(rootView,lp);
		bouncingView.show();

		return this;
	}
	
	public void dismiss(){
		mParentVG.removeView(rootView);
		rootView = null;
	}
}
