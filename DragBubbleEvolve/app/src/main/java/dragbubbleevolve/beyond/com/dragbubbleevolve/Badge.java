package dragbubbleevolve.beyond.com.dragbubbleevolve;

import android.view.View;

/**
 * @author chqiu
 *         Email:qstumn@163.com
 */

public interface Badge {

    Badge setBadgeNumber(int badgeNum);

    int getBadgeNumber();

    Badge setBadgeText(String badgeText);

    String getBadgeText();

    Badge setExactMode(boolean isExact);

    boolean isExactMode();

    Badge setShowShadow(boolean showShadow);

    boolean isShowShadow();

    Badge setBadgeBackgroundSize(int size);

    Badge setBadgeBackgroundColor(int color);


    int getBadgeBackgroundColor();

    Badge setBadgeTextColor(int color);

    int getBadgeTextColor();

    Badge setBadgeTextSize(float size, boolean isSpValue);

    float getBadgeTextSize(boolean isSpValue);

    Badge setBadgePadding(float padding, boolean isDpValue);

    float getBadgePadding(boolean isDpValue);

    boolean isDraggable();

    Badge setBadgeGravity(int gravity);

    Badge setGravityOffset(float offsetX, float offsetY, boolean isDpValue);


    Badge bindTarget(View view);


    Badge setOnDragStateChangedListener(OnDragStateChangedListener l);

    interface OnDragStateChangedListener {
        /**
         * 气泡默认状态--静止
         */
        int BUBBLE_STATE_DEFAUL = 0;
        /**
         * 气泡相连
         */
        int BUBBLE_STATE_CONNECT = 1;
        /**
         * 气泡分离
         */
        int BUBBLE_STATE_APART = 2;
        /**
         * 气泡消失
         */
        int BUBBLE_STATE_DISMISS = 3;

        void onDragStateChanged(int dragState, Badge badge, View targetView);
    }
}
