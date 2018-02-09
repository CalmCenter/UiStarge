package dragbubbleevolve.beyond.com.dragbubbleevolve;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Badge view;
    CheckBox cb,cb_ExactMode;
    private EditText et;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.et);
        cb = (CheckBox) findViewById(R.id.cb);
        cb_ExactMode = (CheckBox) findViewById(R.id.cb_ExactMode);
        btn = (Button) findViewById(R.id.btn);
        view = new DragBubbleView(this)
                .bindTarget(btn)
                .setBadgeNumber(999)//设置数字
//                .setBadgeText("一二一")//设置文字
//                .setBadgeTextColor(getResources().getColor(R.color.green))//文字颜色
//                .setBadgeBackgroundColor(getResources().getColor(R.color.colorAccent))//背景颜色
                .setBadgeTextSize(12, true)//设置文字大小
//                .setBadgePadding(5,true)//设置文字Padding
//                .setBadgeGravity(Gravity.BOTTOM|Gravity.START)//设置Gravity
//                .setGravityOffset(10,10,true)//设置偏移
                .setBadgeBackgroundSize(12)//设置气泡半径  - - 别写太大，会有意想不到的效果
                .setExactMode(true)//设置是否是精确值
                .setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
                    @Override
                    public void onDragStateChanged(int dragState, Badge badge, View targetView) {
                        switch (dragState) {
                            case BUBBLE_STATE_DISMISS:
                                badge.setBadgeNumber(badge.getBadgeNumber() + 1);
                                break;
                        }
                    }
                });//设置监听才可以拖拽
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int num = TextUtils.isEmpty(s) ? 0 : s.toString().length()<9? Integer.parseInt(s.toString()): Integer.parseInt(s.subSequence(0,8).toString());
                view.setBadgeNumber(num);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setShowShadow(isChecked);//设置阴影，需要关闭硬件加速
            }
        });
        cb_ExactMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setExactMode(isChecked);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecyclerViewAty.class));
            }
        });
    }
}
