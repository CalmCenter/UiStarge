package com.practice.customlistview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/6/6.
 *
 * 适配器
 */

public interface BaseTableAdapter {
    public int getRowCount();

    public int getColumnCount();

    public View getView(int row, int column, View convertView, ViewGroup parent);

    public int getWidth(int column);

    public int getHeight(int row);

    public int getItemViewType(int row, int column);

    public int getViewTypeCount();

}
