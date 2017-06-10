package com.practice.customlistview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;


public class TableView extends ViewGroup {
    private BaseTableAdapter adapter;

    private int downX;//滑动时手指落下的X Y
    private int downY;
    private int scrollX;//滑动的距离
    private int scrollY;
    private int firstRow;//当前第一行postiton
    private int firstColumn; //当前第一列position
    private int[] widths;//存放每个View的宽高
    private int[] heights;

    @SuppressWarnings("unused")
    private View headView;//头View 为使用
    private List<View> rowViewList;//保存第一行数据
    private List<View> columnViewList;
    private List<List<View>> bodyViewTable;//表格数据
    private int rowCount;//行数
    private int columnCount;//列数
    private int width;
    private int height;
    private final ImageView[] shadows;//分割的黑线
    private final int shadowSize;//黑线宽度

    private int minimumVelocity;//惯性滑动时最小和最大速率
    private int maximumVelocity;
    private final Flinger flinger;//惯性滑动
    private VelocityTracker velocityTracker;//惯性滑动

    private boolean needRelayout;    //需要重绘标志位
    private int touchSlop;    //滑动最小距离
    private Recycler recycler;//复用相关类

    public TableView(Context context) {
        this(context, null);
    }

    public TableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.headView = null;
        this.rowViewList = new ArrayList<View>();
        this.columnViewList = new ArrayList<View>();
        this.bodyViewTable = new ArrayList<List<View>>();
        this.needRelayout = true;

        this.shadows = new ImageView[4];
        this.shadows[0] = new ImageView(context);
        this.shadows[0].setImageResource(R.drawable.shadow_left);
        this.shadows[1] = new ImageView(context);
        this.shadows[1].setImageResource(R.drawable.shadow_top);
        this.shadows[2] = new ImageView(context);
        this.shadows[2].setImageResource(R.drawable.shadow_right);
        this.shadows[3] = new ImageView(context);
        this.shadows[3].setImageResource(R.drawable.shadow_bottom);

        this.shadowSize = getResources().getDimensionPixelSize(R.dimen.shadow_size);

        this.flinger = new Flinger(context);

        //拿到手机上 最小的滑动距离  判断是点击还是移动的一个最小滑动距离
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int w;
        final int h;

        if (adapter != null) {
            this.rowCount = adapter.getRowCount();//获取数据个数
            this.columnCount = adapter.getColumnCount();
            //
            widths = new int[columnCount + 1];//初始化保存的数组 这里+1 是包括了有一个单向滑动的头部。
            for (int i = -1; i < columnCount; i++) {//这里从-1开始是为了可以添加columnCount + 1条数据
                widths[i + 1] += adapter.getWidth(i);
            }
            heights = new int[rowCount + 1];
            for (int i = -1; i < rowCount; i++) {
                heights[i + 1] += adapter.getHeight(i);
            }

            if (widthMode == MeasureSpec.AT_MOST) {//AT_MOST wrap_content
                //sumArray方法是计算出数组的总和
                w = Math.min(widthSize, sumArray(widths));//判读屏幕宽度和数据宽度，取最小的
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                w = sumArray(widths);
            } else {//具体指或match_parent
                w = widthSize;
                int sumArray = sumArray(widths);
                if (sumArray < widthSize) {//如果 现有view的宽度小于 屏幕宽度 将会把屏幕宽度平分
                    final float factor = widthSize / (float) sumArray;
                    for (int i = 1; i < widths.length; i++) {
                        widths[i] = Math.round(widths[i] * factor);
                    }
                    widths[0] = widthSize - sumArray(widths, 1, widths.length - 1);
                }
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                h = Math.min(heightSize, sumArray(heights));
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                h = sumArray(heights);
            } else {
                h = heightSize;
            }
        } else {
            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                w = 0;
                h = 0;
            } else {
                w = widthSize;
                h = heightSize;
            }
        }
        //必须调用
        setMeasuredDimension(w, h);
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout = false;
            resetTable();//情况所有的集合和View

            if (adapter != null) {
                width = r - l;//屏幕当前的宽度和高度
                height = b - t;
                //画那四条黑线，在实现静态页面是还没什么用
                int left, top, right, bottom;

                right = Math.min(width, sumArray(widths));
                bottom = Math.min(height, sumArray(heights));
                addShadow(shadows[0], widths[0], 0, widths[0] + shadowSize, bottom);
                addShadow(shadows[1], 0, heights[0], right, heights[0] + shadowSize);
                addShadow(shadows[2], right - shadowSize, 0, right, bottom);
                addShadow(shadows[3], 0, bottom - shadowSize, right, bottom);
                //画左上角那个固定的ViewItem(红色部分)
                headView = makeAndSetup(-1, -1, 0, 0, widths[0], heights[0]);
                //画除左上角以外的第一行数据(橘黄色部分)
                left = widths[0];
                //这里用到了源码里的机制，只加载屏幕以内的View
                //当left(当前View的左边<屏幕的宽度才去加载)
                for (int i = firstColumn; i < columnCount && left < width; i++) {
                    //不停的去找下个View的左右边的值
                    right = left + widths[i + 1];
                    final View view = makeAndSetup(-1, i, left, 0, right, heights[0]);
                    rowViewList.add(view);//保存第一行数据
                    left = right;
                }
                //画除左上角以外的第一列数据(棕色部分)
                top = heights[0];
                for (int i = firstRow; i < rowCount && top < height; i++) {
                    bottom = top + heights[i + 1];
                    final View view = makeAndSetup(i, -1, 0, top, widths[0], bottom);
                    columnViewList.add(view);
                    top = bottom;
                }
                //画Body部分(蓝色部分)
                top = heights[0];
                for (int i = firstRow; i < rowCount && top < height; i++) {
                    bottom = top + heights[i + 1];
                    left = widths[0] - scrollX;
                    List<View> list = new ArrayList<View>();
                    for (int j = firstColumn; j < columnCount && left < width; j++) {
                        right = left + widths[j + 1];
                        final View view = makeAndSetup(i, j, left, top, right, bottom);
                        list.add(view);//当前行 一个一个添加  最后相当于一行数据
                        left = right;
                    }
                    bodyViewTable.add(list);//添加一行数据 最后相当于 表格内所有数据
                    top = bottom;
                }

                shadowsVisibility();//分割的黑线
            }
        }
    }
   /*------------------------------------TODO ↓↓↓滑动相关↓↓↓------------------------------------------*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getRawX();
                downY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //拦截move事件  防止子view中有Button一类的控件
                int x2 = Math.abs(downX - (int) ev.getRawX());
                int y2 = Math.abs(downY - (int) ev.getRawY());
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true;
                }
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain(); // then get one
        }
        velocityTracker.addMovement(event); // add this movement to it

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!flinger.isFinished()) { // If scrolling, then stop now
                    flinger.forceFinished();
                }
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int x2 = (int) event.getRawX();
                final int y2 = (int) event.getRawY();
                //如果diffX<0 往右滑
                final int diffX = downX - x2;
                //如果diffy<0 向下滑
                final int diffY = downY - y2;
                downX = x2;
                downY = y2;

                scrollBy(diffX, diffY);
                break;
            }
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                int velocityY = (int) velocityTracker.getYVelocity();

                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(getActualScrollX(), getActualScrollY(), velocityX, velocityY, getMaxScrollX(), getMaxScrollY());
                } else {
                    if (this.velocityTracker != null) { // If the velocity less than threshold
                        this.velocityTracker.recycle(); // recycle the tracker
                        this.velocityTracker = null;
                    }
                }
                break;
            }
        }
        return true;
    }

    private void scrollBounds() {
        scrollX = scrollBounds(scrollX, firstColumn, widths, width);
        scrollY = scrollBounds(scrollY, firstRow, heights, height);
    }

    private int scrollBounds(int desiredScroll, int firstCell, int sizes[], int viewSize) {
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            //修整左滑的临界值
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell));
        } else {
            desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell + 1, sizes.length - 1 - firstCell) + sizes[0] - viewSize));
        }
        return desiredScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (needRelayout) {
            scrollX = x;
            firstColumn = 0;

            scrollY = y;
            firstRow = 0;
        } else {
            scrollBy(x - sumArray(widths, 1, firstColumn) - scrollX, y - sumArray(heights, 1, firstRow) - scrollY);
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollX += x;
        scrollY += y;
        if (needRelayout) {
            return;
        }
        scrollBounds();

        if (scrollX == 0) {
            // no op
        } else if (scrollX > 0) {//向左滑动
            //当scrollX大于body(蓝色区域)内第一个可见View的宽度的时候
            //这里用while是有可能快速移动，直接处理多个View的情况
            while (widths[firstColumn + 1] < scrollX) {
                if (!rowViewList.isEmpty()) {
                    removeLeft();
                }
                scrollX -= widths[firstColumn + 1];
                firstColumn++;
            }
            //如果不快速滑动 这里的rowViewList可以理解为body中可见的View
            //这里的getFilledWidth()其实就是计算出第一列(单向滑动的那列)的宽度+body(蓝色区域)内rowViewList的中保存的所有View的宽度(有可能首尾的View超出去一部分或超出多个View，那部分也算)-scrollX
            //所以这里计算的就是body(蓝色区域)左边到rowViewList的中保存的最后一个View的宽度(最后一个View超出屏幕部分也算)。因为scrollX就是向左滑了的部分，也就是左边超出的部分
            //这里用while是有可能快速移动，直接处理多个View的情况
            while (getFilledWidth() < width) {//这里的判断就是当把最后一个View超出屏幕的部分全部移回来了，就是添加下个view的时候
                addRight();
            }
        } else {//向右滑动第一个View全部出现时调用一次
            //往右滑的时候scrollX是负的。所以getFilledWidth()里的-scrollX 成了 +|scrollX|
            //和上边的一样getFilledWidth()计算的是第一列(单向滑动的那列)的宽度+body(蓝色区域)的第一个view到rowViewList的中保存的最后一个View的宽度(最后一个View超出屏幕部分也算)
            //因为只有在右滑时第一个View全部出现的时候调用一次，所以这里body(蓝色区域)的第一个view就相当于从body的左边开始
            //这里判断就相当于：可见的第一个View到rowViewList的中保存的最后一个
            while (!rowViewList.isEmpty() && getFilledWidth() - widths[firstColumn + rowViewList.size()] >= width) {
                removeRight();
            }
            //当scrollX小于0的时候证明已经 将之前左滑的部分又向右滑回来了
            while (0 > scrollX) {
                addLeft();
                firstColumn--;
                scrollX += widths[firstColumn + 1];
            }
        }

        if (scrollY == 0) {
            // no op
        } else if (scrollY > 0) {
            while (heights[firstRow + 1] < scrollY) {
                if (!columnViewList.isEmpty()) {
                    removeTop();
                }
                scrollY -= heights[firstRow + 1];
                firstRow++;
            }
            while (getFilledHeight() < height) {
                addBottom();
            }
        } else {
            while (!columnViewList.isEmpty() && getFilledHeight() - heights[firstRow + columnViewList.size()] >= height) {
                removeBottom();
            }
            while (0 > scrollY) {
                addTop();
                firstRow--;
                scrollY += heights[firstRow + 1];
            }
        }

        repositionViews();//没有这个体现不出滑动的效果

        shadowsVisibility();//分割线
    }

    private void repositionViews() {
        int left, top, right, bottom, i;

        left = widths[0] - scrollX;
        i = firstColumn;
        for (View view : rowViewList) {
            right = left + widths[++i];
            view.layout(left, 0, right, heights[0]);
            left = right;
        }

        top = heights[0] - scrollY;
        i = firstRow;
        for (View view : columnViewList) {
            bottom = top + heights[++i];
            view.layout(0, top, widths[0], bottom);
            top = bottom;
        }

        top = heights[0] - scrollY;
        i = firstRow;
        for (List<View> list : bodyViewTable) {
            bottom = top + heights[++i];
            left = widths[0] - scrollX;
            int j = firstColumn;
            for (View view : list) {
                right = left + widths[++j];
                view.layout(left, top, right, bottom);
                left = right;
            }
            top = bottom;
        }
        invalidate();
    }

    private int getFilledHeight() {
        return heights[0] + sumArray(heights, firstRow + 1, columnViewList.size()) - scrollY;
    }


    public int getFilledWidth() {
        return widths[0] + sumArray(widths, firstColumn + 1, rowViewList.size()) - scrollX;
    }
    /*------------------------------------TODO ↑↑↑滑动相关↑↑↑------------------------------------------*/

    //获取一个View
    private View makeAndSetup(int row, int column, int left, int top, int right, int bottom) {
        View view = obtainView(row, column, right - left, bottom - top);
        //给子控件边界
        view.layout(left, top, right, bottom);
        return view;
    }

    //真正获取View
    private View obtainView(int row, int column, int width, int height) {
        //得到当前控件的类型
        final int itemType = adapter.getItemViewType(row, column);
        //从回收池拿到一个View  可能为null
        final View reclyView;
        if (itemType == -1) {
            reclyView = null;
        } else {
            reclyView = recycler.getRecyclerView(itemType);
        }
        //recycleView 可能为null
        View view = adapter.getView(row, column, reclyView, this);
        if (view == null) {
            throw new RuntimeException("view 不能为空");
        }
        //view不可能为null
        view.setTag(R.id.tag_type_view, itemType);
        view.setTag(R.id.tag_column, column);
        view.setTag(R.id.tag_row, row);
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                , MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addTableView(view, row, column);
        return view;


    }

    private void addTableView(View view, int row, int column) {
        if (row == -1 && column == -1) {
            addView(view);
        } else if (column == -1 || row == -1) {
            addView(view, getChildCount() - 5);
        } else {
            addView(view, 0);
        }
    }


    private void resetTable() {
        headView = null;
        rowViewList.clear();
        columnViewList.clear();
        bodyViewTable.clear();

        removeAllViews();
    }

    //计算数组的  总和
    private int sumArray(int array[]) {
        return sumArray(array, 0, array.length);
    }

    private int sumArray(int array[], int start, int end) {
        int sum = 0;
        end += start;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public int getActualScrollX() {
        return scrollX + sumArray(widths, 1, firstColumn);
    }

    public int getActualScrollY() {
        return scrollY + sumArray(heights, 1, firstRow);
    }

    private int getMaxScrollX() {
        return Math.max(0, sumArray(widths) - width);
    }

    private int getMaxScrollY() {
        return Math.max(0, sumArray(heights) - height);
    }

    public void setAdapter(BaseTableAdapter adapter) {
        this.adapter = adapter;
        this.recycler = new Recycler(adapter.getViewTypeCount());
        scrollX = 0;
        scrollY = 0;
        firstColumn = 0;
        firstRow = 0;
        needRelayout = true;
        requestLayout();
    }


    private void addShadow(ImageView imageView, int l, int t, int r, int b) {
        imageView.layout(l, t, r, b);
        addView(imageView);
    }

    private void shadowsVisibility() {
        final int actualScrollX = getActualScrollX();
        final int actualScrollY = getActualScrollY();
        final int[] remainPixels = {
                actualScrollX,
                actualScrollY,
                getMaxScrollX() - actualScrollX,
                getMaxScrollY() - actualScrollY,
        };

        for (int i = 0; i < shadows.length; i++) {
            setAlpha(shadows[i], Math.min(remainPixels[i] / (float) shadowSize, 1));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    private void setAlpha(ImageView imageView, float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageView.setAlpha(alpha);
        } else {
            imageView.setAlpha(Math.round(alpha * 255));
        }
    }
   /*------------------------------------TODO ↓↓↓滑动惯性处理类↓↓↓------------------------------------------*/

    // http://stackoverflow.com/a/6219382/842697
    private class Flinger implements Runnable {
        private final Scroller scroller;

        private int lastX = 0;
        private int lastY = 0;

        Flinger(Context context) {
            scroller = new Scroller(context);
        }

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);

            lastX = initX;
            lastY = initY;
            post(this);
        }

        public void run() {
            if (scroller.isFinished()) {
                return;
            }

            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            int diffX = lastX - x;
            int diffY = lastY - y;
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY);
                lastX = x;
                lastY = y;
            }

            if (more) {
                post(this);
            }
        }

        boolean isFinished() {
            return scroller.isFinished();
        }

        void forceFinished() {
            if (!scroller.isFinished()) {
                scroller.forceFinished(true);
            }
        }
    }
	/*------------------------------------TODO ↑↑↑滑动惯性处理类↑↑↑------------------------------------------*/


	/*------------------------------------TODO ↓↓↓滑动时对View的处理 ↓↓↓------------------------------------------*/

    private void addTop() {
        addTopAndBottom(firstRow - 1, 0);
    }

    private void addBottom() {
        final int size = columnViewList.size();
        addTopAndBottom(firstRow + size, size);
    }

    private void addTopAndBottom(int row, int index) {
        View view = obtainView(row, -1, widths[0], heights[row + 1]);
        columnViewList.add(index, view);

        List<View> list = new ArrayList<View>();
        final int size = rowViewList.size() + firstColumn;
        for (int i = firstColumn; i < size; i++) {
            view = obtainView(row, i, widths[i + 1], heights[row + 1]);
            list.add(view);
        }
        bodyViewTable.add(index, list);
    }

    private void removeTop() {
        removeTopOrBottom(0);
    }


    private void removeBottom() {
        removeTopOrBottom(columnViewList.size() - 1);
    }


    private void removeTopOrBottom(int position) {
        removeView(columnViewList.remove(position));
        List<View> remove = bodyViewTable.remove(position);
        for (View view : remove) {
            removeView(view);
        }
    }

    //移除最后一列
    private void removeRight() {
        removeLeftOrRight(rowViewList.size() - 1);
    }

    private void addLeft() {
        addLeftOrRight(firstColumn - 1, 0);
    }

    private void addRight() {
        int size = rowViewList.size();
        addLeftOrRight(firstColumn + size, size);
    }

    private void addLeftOrRight(int column, int index) {
        //添加首行(标题行)右边的view
        View view = obtainView(-1, column, widths[column + 1], heights[0]);
        //更新rowViewList
        rowViewList.add(index, view);
        int i = firstRow;
        //添加body右边的一列view
        for (List<View> list : bodyViewTable) {
            view = obtainView(i, column, widths[column + 1], heights[i + 1]);
            list.add(index, view);
            i++;
        }
    }

    private void removeLeft() {
        removeLeftOrRight(0);
    }

    private void removeLeftOrRight(int i) {
        removeView(rowViewList.remove(i));
        //移除
        for (List<View> list : bodyViewTable) {
            removeView(list.remove(i));
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        final int typeView = (Integer) view.getTag(R.id.tag_type_view);
        recycler.addRecycledView(view, typeView);
    }
	/*------------------------------------TODO ↑↑↑滑动时对View的处理↑↑↑------------------------------------------*/

}
