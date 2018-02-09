package dragbubbleevolve.beyond.com.dragbubbleevolve;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by John on 2017/5/22.
 */

public class DragBubbleView extends View implements Badge {

    /**
     * 气泡默认状态--静止
     */
    private final int BUBBLE_STATE_DEFAUL = 0;
    /**
     * 气泡相连
     */
    private final int BUBBLE_STATE_CONNECT = 1;
    /**
     * 气泡分离
     */
    private final int BUBBLE_STATE_APART = 2;
    /**
     * 气泡消失
     */
    private final int BUBBLE_STATE_DISMISS = 3;

    /**
     * 气泡半径
     */
    private float mBubbleRadius;
    /**
     * 气泡颜色
     */
    private int mBubbleColor;
    /**
     * 气泡消息文字
     */
    private String mTextStr;
    /**
     * 气泡显示的数字
     */
    private int mBadgeNumber;
    /**
     * 是否精准显示
     */
    private boolean mExact;
    /**
     * 气泡消息文字颜色
     */
    private int mTextColor;
    /**
     * 气泡消息文字大小
     */
    private float mTextSize;
    /**
     * 不动气泡的半径
     */
    private float mBubStillRadius;
    /**
     * 可动气泡的半径
     */
    private float mBubMoveableRadius;
    /**
     * 不动气泡的圆心
     */
    private PointF mBubStillCenter;
    /**
     * 可动气泡的圆心
     */
    private PointF mBubMoveableCenter;
    /**
     * 气泡的画笔
     */
    private Paint mBubblePaint;
    /**
     * 贝塞尔曲线path
     */
    private Path mBezierPath;


    private TextPaint mBadgeTextPaint;


    private Paint mBurstPaint;

    private Rect mBurstRect;

    /**
     * 气泡状态标志
     */
    private int mBubbleState = BUBBLE_STATE_DEFAUL;
    /**
     * 两气泡圆心距离
     */
    private float mDist;
    /**
     * 气泡相连状态最大圆心距离
     */
    private float mMaxDist;
    /**
     * 手指触摸偏移量
     */
    private final float MOVE_OFFSET;

    /**
     * 气泡爆炸的bitmap数组
     */
    private Bitmap[] mBurstBitmapsArray;
    /**
     * 是否在执行气泡爆炸动画
     */
    private boolean mIsBurstAnimStart = false;

    /**
     * 当前气泡爆炸图片index
     */
    private int mCurDrawableIndex;

    /**
     * 设置阴影
     */
    private boolean mShowShadow;

    /**
     * 气泡爆炸的图片id数组
     */
    private int[] mBurstDrawablesArray = {R.drawable.burst_1, R.drawable.burst_2
            , R.drawable.burst_3, R.drawable.burst_4, R.drawable.burst_5};


    private Paint.FontMetrics mBadgeTextFontMetrics;
    private RectF mBadgeTextRect;
    private RectF mBadgeBackgroundRect;
    private float mBadgePadding;

    private float mGravityOffsetX;
    private float mGravityOffsetY;
    private int mWidth;
    private int mHeight;
    private int mBadgeGravity;

    private View mTargetView;
    private ViewGroup mActivityRoot;
    private int mDragQuadrant;

    /**
     * 是否可以拖拽
     */
    private boolean mDraggable;
    private OnDragStateChangedListener mDragStateChangedListener;

    public DragBubbleView(Context context) {
        this(context, null);
    }

    public DragBubbleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView, defStyleAttr, 0);
            mBubbleRadius = array.getDimension(R.styleable.DragBubbleView_bubble_radius, mBubbleRadius);
            mBubbleColor = array.getColor(R.styleable.DragBubbleView_bubble_color, Color.RED);
            mTextStr = array.getString(R.styleable.DragBubbleView_bubble_text);
            mTextSize = array.getDimension(R.styleable.DragBubbleView_bubble_textSize, mTextSize);
            mTextColor = array.getColor(R.styleable.DragBubbleView_bubble_textColor, Color.WHITE);
            array.recycle();
        }
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mBubbleRadius = (float) DisplayUtil.dp2px(context, 10);
        mTextSize = (float) DisplayUtil.dp2px(context, 11);
        mTextSize = (float) DisplayUtil.dp2px(context, 11);
        mBubbleColor = 0xFFE84E40;
        mTextColor = 0xFFFFFFFF;
        mBubStillRadius = (float) DisplayUtil.dp2px(context, 10);
        mBubMoveableRadius = mBubStillRadius;
        mMaxDist = 8 * mBubbleRadius;
        MOVE_OFFSET = mMaxDist / 4;

        //抗锯齿
        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setColor(mBubbleColor);
        mBubblePaint.setAntiAlias(true);
        mBubblePaint.setStyle(Paint.Style.FILL);
        mBezierPath = new Path();

        mBadgeTextPaint = new TextPaint();
        mBadgeTextPaint.setAntiAlias(true);
        mBadgeTextPaint.setSubpixelText(true);
        mBadgeTextPaint.setFakeBoldText(true);
        mBadgeTextPaint.setColor(0xFFFFFFFF);
        mBadgeTextPaint.setTextAlign(Paint.Align.CENTER);
        mBadgeTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mBadgeTextRect = new RectF();
        mBadgeBackgroundRect = new RectF();
        mBadgePadding = DisplayUtil.dp2px(getContext(), 5);
        mGravityOffsetX = DisplayUtil.dp2px(getContext(), 1);
        mGravityOffsetY = DisplayUtil.dp2px(getContext(), 1);
        mBadgeGravity = Gravity.END | Gravity.TOP;


        mBubStillCenter = new PointF();
        mBubMoveableCenter = new PointF();

        mBurstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBurstPaint.setFilterBitmap(true);
        mBurstRect = new Rect();
        mBurstBitmapsArray = new Bitmap[mBurstDrawablesArray.length];
        for (int i = 0; i < mBurstDrawablesArray.length; i++) {
            //将气泡爆炸的drawable转为bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mBurstDrawablesArray[i]);
            mBurstBitmapsArray[i] = bitmap;
        }

        measureText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslationZ(1000);
        }
    }

    /**
     * 测量文本占用大小
     */
    private void measureText() {
        mBadgeTextRect.left = 0;
        mBadgeTextRect.top = 0;
        if (TextUtils.isEmpty(mTextStr)) {
            mBadgeTextRect.right = 0;
            mBadgeTextRect.bottom = 0;
        } else {
            mBadgeTextPaint.setTextSize(mTextSize);
            mBadgeTextRect.right = mBadgeTextPaint.measureText(mTextStr);
            mBadgeTextFontMetrics = mBadgeTextPaint.getFontMetrics();
            mBadgeTextRect.bottom = mBadgeTextFontMetrics.descent - mBadgeTextFontMetrics.ascent;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //手指下落
            case MotionEvent.ACTION_DOWN: {
                //当状态不为消失的时候
                if (mBubbleState != BUBBLE_STATE_DISMISS && mBubbleState != BUBBLE_STATE_CONNECT && mDraggable) {
                    initRowBadgeCenter();
                    //获取下落点到小圆的距离
                    mDist = (float) Math.hypot(event.getRawX() - mBubStillCenter.x,
                            event.getRawY() - mBubStillCenter.y);
                    //如果距离小于 圆半径+偏移量
                    if (mDist < mBubbleRadius + MOVE_OFFSET) {
                        // 加上MOVE_OFFSET是为了方便拖拽
                        mBubbleState = BUBBLE_STATE_CONNECT;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        screenFromWindow(true);
                        mBubMoveableCenter.x = event.getRawX();
                        mBubMoveableCenter.y = event.getRawY();

                    } else {
                        mBubbleState = BUBBLE_STATE_DEFAUL;
                        return false;
                    }

                } else {
                    return false;
                }

            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (mBubbleState != BUBBLE_STATE_DEFAUL && mBubbleState != BUBBLE_STATE_DISMISS) {
                    //改变大圆圆心
                    mBubMoveableCenter.x = event.getRawX();
                    mBubMoveableCenter.y = event.getRawY();
                    //计算圆心距
                    mDist = (float) Math.hypot(event.getRawX() - mBubStillCenter.x,
                            event.getRawY() - mBubStillCenter.y);

                    if (mBubbleState == BUBBLE_STATE_CONNECT) {

                        // 减去MOVE_OFFSET是为了让不动气泡半径到一个较小值时就直接消失
                        // 或者说是进入分离状态
                        if (mDist < mMaxDist - MOVE_OFFSET) {
                            //根据圆心距缩小 小圆的半径
                            mBubStillRadius = mBubbleRadius - mDist / 8;
                        } else {
                            mBubbleState = BUBBLE_STATE_APART;
                        }
                    }
                    invalidate();
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                //如果是相连状态
                if (mBubbleState == BUBBLE_STATE_CONNECT) {
                    startBubbleRestAnim();
                } else if (mBubbleState == BUBBLE_STATE_APART) {//如果是分离状态

                    //是否返回原点还是爆炸~
                    if (mDist < 2 * mBubbleRadius) {
                        startBubbleRestAnim();
                    } else {

                        startBubbleBurstAnim();
                    }
                }
            }
            break;
        }
        return true;
    }

    private void startBubbleBurstAnim() {
        //气泡改为消失状态
        mBubbleState = BUBBLE_STATE_DISMISS;
        mIsBurstAnimStart = true;
        //做一个int型属性动画，从0~mBurstDrawablesArray.length结束
        ValueAnimator anim = ValueAnimator.ofInt(0, mBurstDrawablesArray.length);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //设置当前绘制的爆炸图片index
                mCurDrawableIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reset();
                updataListener(OnDragStateChangedListener.BUBBLE_STATE_DISMISS);
                //修改动画执行标志
                mIsBurstAnimStart = false;
            }
        });
        anim.start();

    }

    private void startBubbleRestAnim() {
        mBubbleState = BUBBLE_STATE_APART;

        ValueAnimator anim = ValueAnimator.ofObject(new PointFEvaluator(),
                new PointF(mBubMoveableCenter.x, mBubMoveableCenter.y),
                new PointF(mBubStillCenter.x, mBubStillCenter.y));

        anim.setDuration(100);
        anim.setInterpolator(new OvershootInterpolator(5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBubMoveableCenter = (PointF) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBubbleState = BUBBLE_STATE_DEFAUL;
                reset();
            }
        });
        anim.start();
    }


    private void initPaints() {
        mDragQuadrant = MathUtil.getQuadrant(mBubMoveableCenter, mBubStillCenter);
        showShadowImpl(mShowShadow);
        mBubblePaint.setColor(mBubbleColor);
        mBadgeTextPaint.setColor(mTextColor);
        mBadgeTextPaint.setTextAlign(Paint.Align.CENTER);
        mBubStillRadius = mBubbleRadius;
        mBubMoveableRadius = mBubStillRadius;
        mMaxDist = 8 * mBubbleRadius;
    }

    boolean flag;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mTextStr.equals("")) {

            //计算气泡中心点
            if (mBubbleState == BUBBLE_STATE_DEFAUL && !flag) {
                findBadgeCenter();
                flag = true;
            }


            // 2、画相连的气泡状态
            if (mBubbleState == BUBBLE_STATE_CONNECT) {

                // 1、画静止气泡
                canvas.drawCircle(mBubStillCenter.x, mBubStillCenter.y,
                        mBubStillRadius, mBubblePaint);
                // 2、画相连曲线
                // 计算控制点坐标，两个圆心的中点
                int iAnchorX = (int) ((mBubStillCenter.x + mBubMoveableCenter.x) / 2);
                int iAnchorY = (int) ((mBubStillCenter.y + mBubMoveableCenter.y) / 2);

                float cosTheta = (mBubMoveableCenter.x - mBubStillCenter.x) / mDist;
                float sinTheta = (mBubMoveableCenter.y - mBubStillCenter.y) / mDist;

                //A
                float iBubStillStartX = mBubStillCenter.x - mBubStillRadius * sinTheta;
                float iBubStillStartY = mBubStillCenter.y + mBubStillRadius * cosTheta;
                //B
                float iBubMoveableEndX = mBubMoveableCenter.x - mBubMoveableRadius * sinTheta;
                float iBubMoveableEndY = mBubMoveableCenter.y + mBubMoveableRadius * cosTheta;
                //C
                float iBubMoveableStartX = mBubMoveableCenter.x + mBubMoveableRadius * sinTheta;
                float iBubMoveableStartY = mBubMoveableCenter.y - mBubMoveableRadius * cosTheta;
                //D
                float iBubStillEndX = mBubStillCenter.x + mBubStillRadius * sinTheta;
                float iBubStillEndY = mBubStillCenter.y - mBubStillRadius * cosTheta;

                mBezierPath.reset();
                // 画上半弧
                mBezierPath.moveTo(iBubStillStartX, iBubStillStartY);
                mBezierPath.quadTo(iAnchorX, iAnchorY, iBubMoveableEndX, iBubMoveableEndY);
                // 画下半弧
                mBezierPath.lineTo(iBubMoveableStartX, iBubMoveableStartY);
                mBezierPath.quadTo(iAnchorX, iAnchorY, iBubStillEndX, iBubStillEndY);
                mBezierPath.close();
                canvas.drawPath(mBezierPath, mBubblePaint);
            }

            // 3、画消失状态---爆炸动画

            if (mIsBurstAnimStart) {
                mBurstRect.set((int) (mBubMoveableCenter.x - mBubMoveableRadius),
                        (int) (mBubMoveableCenter.y - mBubMoveableRadius),
                        (int) (mBubMoveableCenter.x + mBubMoveableRadius),
                        (int) (mBubMoveableCenter.y + mBubMoveableRadius));

                canvas.drawBitmap(mBurstBitmapsArray[mCurDrawableIndex], null,
                        mBurstRect, mBubblePaint);
            }

            /**
             * 最上层的圆放在最后画
             */
            // 1、画拖拽的气泡 和 文字
            if (mBubbleState != BUBBLE_STATE_DISMISS) {
                //重置画笔属性
                initPaints();
                DrawBubMoveable(canvas, mBubMoveableCenter, mBubMoveableRadius);
            }
        }

    }

    public void DrawBubMoveable(Canvas canvas, PointF center, float radius) {
        if (mTextStr.isEmpty() || mTextStr.length() == 1) {
            //数字为一位的时候
            mBadgeBackgroundRect.left = center.x - (int) radius;
            mBadgeBackgroundRect.top = center.y - (int) radius;
            mBadgeBackgroundRect.right = center.x + (int) radius;
            mBadgeBackgroundRect.bottom = center.y + (int) radius;
            canvas.drawCircle(center.x, center.y, radius, mBubblePaint);
        } else {
            mBadgeBackgroundRect.left = center.x - (mBadgeTextRect.width() / 2f + mBadgePadding);
            mBadgeBackgroundRect.top = center.y - (mBadgeTextRect.height() / 2f + mBadgePadding * 0.5f);
            mBadgeBackgroundRect.right = center.x + (mBadgeTextRect.width() / 2f + mBadgePadding);
            mBadgeBackgroundRect.bottom = center.y + (mBadgeTextRect.height() / 2f + mBadgePadding * 0.5f);

            radius = mBadgeBackgroundRect.height() / 2f;
            mBubMoveableRadius = radius;
            canvas.drawRoundRect(mBadgeBackgroundRect, radius, radius, mBubblePaint);
        }


        if (!mTextStr.isEmpty()) {
            canvas.drawText(mTextStr, center.x,
                    (mBadgeBackgroundRect.bottom + mBadgeBackgroundRect.top
                            - mBadgeTextFontMetrics.bottom - mBadgeTextFontMetrics.top) / 2f,
                    mBadgeTextPaint);
            //基线
            /*canvas.drawCircle(center.x,
                    (mBadgeBackgroundRect.bottom + mBadgeBackgroundRect.top) / 2 + (
                            mBadgeTextFontMetrics.bottom - mBadgeTextFontMetrics.top) / 2f - mBadgeTextFontMetrics.bottom, 10.0f,
                    mBadgeTextPaint);*/
        }
    }

    /**
     * 计算禁止时动圆的中心点
     */
    private void findBadgeCenter() {
        mBubbleState = BUBBLE_STATE_DEFAUL;
        float rectWidth = mBadgeTextRect.height() > mBadgeTextRect.width() ?
                mBadgeTextRect.height() : mBadgeTextRect.width();
        switch (mBadgeGravity) {
            case Gravity.START | Gravity.TOP:
                mBubMoveableCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f;
                mBubMoveableCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f;
                break;
            case Gravity.START | Gravity.BOTTOM:
                mBubMoveableCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f;
                mBubMoveableCenter.y = mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f);
                break;
            case Gravity.END | Gravity.TOP:
                mBubMoveableCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f);
                mBubMoveableCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f;
                break;
            case Gravity.END | Gravity.BOTTOM:
                mBubMoveableCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f);
                mBubMoveableCenter.y = mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f);
                break;
            case Gravity.CENTER:
                mBubMoveableCenter.x = mWidth / 2f;
                mBubMoveableCenter.y = mHeight / 2f;
                break;
            case Gravity.CENTER | Gravity.TOP:
                mBubMoveableCenter.x = mWidth / 2f;
                mBubMoveableCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f;
                break;
            case Gravity.CENTER | Gravity.BOTTOM:
                mBubMoveableCenter.x = mWidth / 2f;
                mBubMoveableCenter.y = mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f);
                break;
            case Gravity.CENTER | Gravity.START:
                mBubMoveableCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f;
                mBubMoveableCenter.y = mHeight / 2f;
                break;
            case Gravity.CENTER | Gravity.END:
                mBubMoveableCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f);
                mBubMoveableCenter.y = mHeight / 2f;
                break;
        }
        initRowBadgeCenter();
    }

    /**
     * 计算不动圆 在全屏的坐标
     */
    private void initRowBadgeCenter() {
        int[] screenPoint = new int[2];
        getLocationOnScreen(screenPoint);
        mBubStillCenter.x = mBubMoveableCenter.x + screenPoint[0];
        mBubStillCenter.y = mBubMoveableCenter.y + screenPoint[1];
    }

    public void reset() {
        screenFromWindow(false);
        getParent().requestDisallowInterceptTouchEvent(false);
        flag = false;
        invalidate();
    }


    /**
     * 设置数量
     */
    @Override
    public Badge setBadgeNumber(int badgeNum) {
        mBubbleState = BUBBLE_STATE_DEFAUL;
        flag = false;
        mBadgeNumber = badgeNum;
        if (badgeNum < 0) {
            mTextStr = "";
        } else if (badgeNum > 99) {
            mTextStr = mExact ? String.valueOf(badgeNum) : "99+";
        } else if (badgeNum > 0 && badgeNum <= 99) {
            mTextStr = String.valueOf(badgeNum);
        } else if (badgeNum == 0) {
            mTextStr = "";
        }
        measureText();
        invalidate();
        return this;
    }

    /**
     * 显示阴影
     */
    private void showShadowImpl(boolean showShadow) {
        int x = DisplayUtil.dp2px(getContext(), 1);
        int y = DisplayUtil.dp2px(getContext(), 1.5f);
        switch (mDragQuadrant) {
            case 1:
                x = DisplayUtil.dp2px(getContext(), 1);
                y = DisplayUtil.dp2px(getContext(), -1.5f);
                break;
            case 2:
                x = DisplayUtil.dp2px(getContext(), -1);
                y = DisplayUtil.dp2px(getContext(), -1.5f);
                break;
            case 3:
                x = DisplayUtil.dp2px(getContext(), -1);
                y = DisplayUtil.dp2px(getContext(), 1.5f);
                break;
            case 4:
                x = DisplayUtil.dp2px(getContext(), 1);
                y = DisplayUtil.dp2px(getContext(), 1.5f);
                break;
        }
        mBubblePaint.setShadowLayer(showShadow ? DisplayUtil.dp2px(getContext(), 2f)
                : 0, x, y, 0x33000000);
    }

    /**
     * 获取数量
     */
    @Override
    public int getBadgeNumber() {
        return mBadgeNumber;
    }

    /**
     * 设置文字
     */
    @Override
    public Badge setBadgeText(String badgeText) {
        mTextStr = badgeText;
        mBadgeNumber = 1;
        measureText();
        invalidate();
        return this;
    }

    /**
     * 获取文字
     */
    @Override
    public String getBadgeText() {
        return mTextStr;
    }

    /**
     * 是否显示精确值
     */
    @Override
    public Badge setExactMode(boolean isExact) {
        mExact = isExact;
        if (mBadgeNumber > 99) {
            setBadgeNumber(mBadgeNumber);
        }
        return this;
    }


    @Override
    public boolean isExactMode() {
        return mExact;
    }

    /**
     * 是否显示阴影
     */
    @Override
    public Badge setShowShadow(boolean showShadow) {
        mShowShadow = showShadow;
        invalidate();
        return this;
    }


    @Override
    public boolean isShowShadow() {
        return mShowShadow;
    }


    /**
     * 设置气泡背景颜色
     */
    @Override
    public Badge setBadgeBackgroundColor(int color) {
        mBubbleColor = color;
        if (mBubbleColor == Color.TRANSPARENT) {
            mBadgeTextPaint.setXfermode(null);
        } else {
//            mBadgeTextPaint.setXfermode(null);
            mBadgeTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
        invalidate();
        return this;
    }

    @Override
    public Badge setBadgeBackgroundSize(int size) {
        mBubbleRadius = (float) DisplayUtil.dp2px(getContext(), size);
        invalidate();
        return this;
    }

    @Override
    public int getBadgeBackgroundColor() {
        return mBubbleColor;
    }

    /**
     * 设置文字颜色
     */
    @Override
    public Badge setBadgeTextColor(int color) {
        mTextColor = color;
        invalidate();
        return this;
    }

    @Override
    public int getBadgeTextColor() {
        return mTextColor;
    }

    /**
     * 设置文字大小
     */
    @Override
    public Badge setBadgeTextSize(float size, boolean isSpValue) {
        mTextSize = isSpValue ? DisplayUtil.dp2px(getContext(), size) : size;
        measureText();
        invalidate();
        return this;
    }

    @Override
    public float getBadgeTextSize(boolean isSpValue) {
        return mTextSize;
    }

    /**
     * 设置气泡的Padding
     */
    @Override
    public Badge setBadgePadding(float padding, boolean isDpValue) {
        mBadgePadding = isDpValue ? DisplayUtil.dp2px(getContext(), padding) : padding;
        invalidate();
        return this;
    }

    @Override
    public float getBadgePadding(boolean isDpValue) {
        return mBadgePadding;
    }

    /**
     * 是否可以拖拽
     */
    @Override
    public boolean isDraggable() {
        return mDraggable;
    }

    /**
     * 位置
     *
     * @param gravity only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP ,
     *                Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM ,
     *                Gravity.CENTER , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ,
     *                Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END
     */
    @Override
    public Badge setBadgeGravity(int gravity) {
        if (gravity == (Gravity.START | Gravity.TOP) ||
                gravity == (Gravity.END | Gravity.TOP) ||
                gravity == (Gravity.START | Gravity.BOTTOM) ||
                gravity == (Gravity.END | Gravity.BOTTOM) ||
                gravity == (Gravity.CENTER) ||
                gravity == (Gravity.CENTER | Gravity.TOP) ||
                gravity == (Gravity.CENTER | Gravity.BOTTOM) ||
                gravity == (Gravity.CENTER | Gravity.START) ||
                gravity == (Gravity.CENTER | Gravity.END)) {
            mBadgeGravity = gravity;
            invalidate();
        } else {
            throw new IllegalStateException("only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP , " +
                    "Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM , Gravity.CENTER" +
                    " , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ," +
                    "Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END");
        }
        return this;
    }

    /**
     * 设置偏移
     */
    @Override
    public Badge setGravityOffset(float offsetX, float offsetY, boolean isDpValue) {
        mGravityOffsetX = isDpValue ? DisplayUtil.dp2px(getContext(), offsetX) : offsetX;
        mGravityOffsetY = isDpValue ? DisplayUtil.dp2px(getContext(), offsetY) : offsetY;
        invalidate();
        return this;
    }

    private void updataListener(int state) {
        if (mDragStateChangedListener != null)
            mDragStateChangedListener.onDragStateChanged(state, this, mTargetView);
    }

    /**
     * 设置气泡监听，设置监听才可以拖动
     */
    @Override
    public Badge setOnDragStateChangedListener(OnDragStateChangedListener l) {
        mDraggable = l != null;
        mDragStateChangedListener = l;
        return this;
    }


    /**
     * TODO -----------------↓↓↓添加到父View的逻辑↓↓↓--------------------------------------------------
     **/
    //当view被附着到一个窗口时触发
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTargetView != null) {//列表中防止初始化了没有设置依附的控件
            if (mActivityRoot == null) findViewRoot(mTargetView);
        }
    }


    private void findViewRoot(View view) {
        mActivityRoot = (ViewGroup) view.getRootView();
        if (mActivityRoot == null) {
            findActivityRoot(view);
        }
    }


    private void findActivityRoot(View view) {
        /*if (view.getParent() != null && view.getParent() instanceof View) {
            findActivityRoot((View) view.getParent());
        } else if (view instanceof ViewGroup) {
            mActivityRoot = (ViewGroup) view;
        }*/
        if (view.getParent() != null) {
            findActivityRoot((View) view.getParent());
        } else if (view instanceof FrameLayout && view.getId()==android.R.id.content) {
            mActivityRoot = (ViewGroup) view;
        }
    }


    @Override
    public Badge bindTarget(final View targetView) {
        if (targetView == null) {
            throw new IllegalStateException("targetView can not be null");
        }
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }

        ViewParent targetParent = targetView.getParent();
        if (targetParent != null && targetParent instanceof ViewGroup) {
            mTargetView = targetView;
            if (targetParent instanceof BadgeContainer) {
                ((BadgeContainer) targetParent).addView(this);
            } else {
                ViewGroup targetContainer = (ViewGroup) targetParent;
                int index = targetContainer.indexOfChild(targetView);
                //
                ViewGroup.LayoutParams targetParams = targetView.getLayoutParams();
                targetContainer.removeView(targetView);
                final BadgeContainer badgeContainer = new BadgeContainer(getContext());
                if (targetContainer instanceof RelativeLayout) {
                    badgeContainer.setId(targetView.getId());
                }
                targetContainer.addView(badgeContainer, index, targetParams);
                badgeContainer.addView(targetView);
                badgeContainer.addView(this);
            }
        } else {
            throw new IllegalStateException("targetView must have a parent");
        }
        return this;
    }


    protected void screenFromWindow(boolean screen) {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (screen) {
            //添加到最外层父类
            mActivityRoot.addView(this, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        } else {
            //添加控件上
            bindTarget(mTargetView);
        }
    }


    private class BadgeContainer extends ViewGroup {

        @Override
        protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
            if (!(getParent() instanceof RelativeLayout)) {
                super.dispatchRestoreInstanceState(container);
            }
        }

        public BadgeContainer(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            View targetView = null, badgeView = null;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!(child instanceof DragBubbleView)) {
                    //拿到依附控件
                    targetView = child;
                } else {
                    badgeView = child;
                }
            }
            if (targetView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                targetView.measure(widthMeasureSpec, heightMeasureSpec);
                if (badgeView != null) {
                    badgeView.measure(MeasureSpec.makeMeasureSpec(targetView.getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(targetView.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
                setMeasuredDimension(targetView.getMeasuredWidth(), targetView.getMeasuredHeight());
            }
        }
    }
    /** TODO -----------------↑↑↑添加到父View的逻辑↑↑↑-------------------------------------------------- **/

}
