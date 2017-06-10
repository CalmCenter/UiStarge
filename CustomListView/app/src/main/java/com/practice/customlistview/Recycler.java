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

    public void addRecycledView(View view,int type){
        views[type].push(view);
    }

    public View getRecyclerView(int type){
        try {
            return views[type].pop();
        } catch (Exception e) {
            return null;
        }
    }
}
