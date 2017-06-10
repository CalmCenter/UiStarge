package com.practice.customlistview;

import android.view.View;

import java.util.Stack;


public class Recycler {
    private Stack<View>[] views;
    public Recycler(int type) {
        views=new Stack[type];
        for (int i = 0; i < type; i++) {
            views[i]=new Stack<View>();
        }
    }
    public void addRecycledView(View view,int type){//滑动时就会调用
        views[type].push(view);//根据ItemType添加View
    }
    public View getRecyclerView(int type){//静态页面是就会调用
        try {//一定要try catch 因为type第一次出现时可能还没有添加过
            return views[type].pop();//根据ItemType拿到View
        } catch (Exception e) {
            return null;
        }
    }
}
