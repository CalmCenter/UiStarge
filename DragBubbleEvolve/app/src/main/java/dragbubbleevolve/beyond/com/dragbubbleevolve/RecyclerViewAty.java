package dragbubbleevolve.beyond.com.dragbubbleevolve;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/31.
 *
 * @author Lee
 */

public class RecyclerViewAty extends AppCompatActivity {

    RecyclerView rv;
    ArrayList<Boolean> data;
    BaseQuickAdapter<Boolean, BaseViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_recycler);

        rv = (RecyclerView) findViewById(R.id.rv);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            if (i%5==0) {
                data.add(true);
            }else {
                data.add(false);
            }
        }
        adapter = new BaseQuickAdapter<Boolean, BaseViewHolder>(R.layout.apt_rv, data){
            @Override
            protected void convert(final BaseViewHolder helper,Boolean item){
                TextView textView = helper.getView(R.id.tv_text);
                textView.setText(String.valueOf(helper.getLayoutPosition() + "item"));
                DragBubbleView dragBubbleView = helper.getView(R.id.drag);
//                DragBubbleView dragBubbleView=new DragBubbleView(RecyclerViewAty.this);
                if (item) {
                    dragBubbleView.setVisibility(View.VISIBLE);
                    dragBubbleView
                            .bindTarget(textView)
                            .setBadgeNumber(100+helper.getLayoutPosition())//设置数字
                            .setBadgeTextSize(10, true)//设置文字大小
                            .setBadgeBackgroundSize(10)
                            .setExactMode(true)//设置是否是精确值
                            .setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
                                @Override
                                public void onDragStateChanged(int dragState, Badge badge, View targetView) {
                                    switch (dragState) {
                                        case BUBBLE_STATE_DISMISS:
                                            getData().set(helper.getLayoutPosition(),false);
                                            break;
                                    }
                                }
                            });
                } else {
                    dragBubbleView.setVisibility(View.GONE);
                }

            }
        };
        rv.setAdapter(adapter);
    }
}
