package com.beyond.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2017/5/5.
 */

public class FlowLayout extends ViewGroup {
    /**
     * 用来保存每行views的列表
     */
    private List<List<View>> mViewLinesList = new ArrayList<>();
    /**
     * 用来保存行高的列表
     */
    private List<Integer> mLineHeights = new ArrayList<>();

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取父容器为FlowLayout设置的测量模式和大小
        int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int iHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int iWidthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int iHeightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWith = 0;
        int measuredHeight = 0;
        int iCurLineW = 0;//记录行宽
        int iCurLineH = 0;//记录行高

        int iChildWidth;
        int iChildHeight;
        int childCount = getChildCount();
        List<View> viewList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == View.GONE) {
                continue;
            }
            // 测量每一个child的宽和高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            // 当前子空间实际占据的宽度
            iChildWidth = childView.getMeasuredWidth() + layoutParams.leftMargin +
                    layoutParams.rightMargin;
            // 当前子空间实际占据的高度
            iChildHeight = childView.getMeasuredHeight() + layoutParams.topMargin +
                    layoutParams.bottomMargin;
            /**
             * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，累加height 然后开启新行
             */
            if (iCurLineW + iChildWidth > iWidthSpecSize) {
                /**1、记录当前行的信息***/
                //1、记录当前行的最大宽度，高度累加
                measuredWith = Math.max(measuredWith, iCurLineW);
                measuredHeight += iCurLineH;
                //2、将当前行的viewList添加至总的mViewsList，将行高添加至总的行高List
                mViewLinesList.add(viewList);
                mLineHeights.add(iCurLineH);

                /**2、记录新一行的信息***/

                //1、重新赋值新一行的宽、高
                iCurLineW = iChildWidth;
                iCurLineH = iChildHeight;

                // 2、新建一行的viewlist，添加新一行的view
                viewList = new ArrayList<View>();
                viewList.add(childView);
            } else {
                // 记录某行内的消息
                //1、行内宽度的叠加、高度比较
                iCurLineW += iChildWidth;
                iCurLineH = Math.max(iCurLineH, iChildHeight);

                // 2、添加至当前行的viewList中
                viewList.add(childView);
            }
            /*****3、如果是最后一个需要记录这行的值**********/
            if (i == childCount - 1) {
                //1、记录当前行的最大宽度，高度累加
                measuredWith = Math.max(measuredWith, iCurLineW);
                measuredHeight += iCurLineH;

                //2、将当前行的viewList添加至总的mViewsList，将行高添加至总的行高List
                mViewLinesList.add(viewList);
                mLineHeights.add(iCurLineH);
            }
        }
        // 最终目的
        setMeasuredDimension((iWidthMode == MeasureSpec.EXACTLY) ? iWidthSpecSize
                : measuredWith, (iHeightMode == MeasureSpec.EXACTLY) ? iHeightSpecSize
                : measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, top, right, bottom;
        int curTop = 0;
        int curLeft = 0;
        int lineCount = mViewLinesList.size();
        for (int i = 0; i < lineCount; i++) {
            List<View> viewList = mViewLinesList.get(i);
            int lineViewSize = viewList.size();
            for (int j = 0; j < lineViewSize; j++) {
                View childView = viewList.get(j);
                if (childView.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                left = curLeft + layoutParams.leftMargin;
                //下对齐 如果不需要 把(mLineHeights.get(i)-childView.getMeasuredHeight())删掉就好
                top = curTop + (mLineHeights.get(i) - childView.getMeasuredHeight()) - layoutParams.topMargin;
                right = left + childView.getMeasuredWidth();
                bottom = top + childView.getMeasuredHeight();
                childView.layout(left, top, right, bottom);
                curLeft += childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }
            curLeft = 0;
            curTop += mLineHeights.get(i);
        }
        mViewLinesList.clear();
        mLineHeights.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int index);
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            final int finalI = i;
            childView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, finalI);
                }
            });
        }

    }
}
