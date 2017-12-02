package com.beyond.contentmenu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


	private BouncingMenu bouncingMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		Toast.makeText(this, "dddd", 1).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			if(bouncingMenu!=null){
				bouncingMenu.dismiss();
				bouncingMenu=null;
			}else{
				//弹出菜单
	//			Toast.makeText(this, "dddd", 1).show();
				List<String> list= new ArrayList<>();
				for (int i = 0; i < 50; i++) {
					list.add("item:"+i);
				}
				MyRecyclerAdapter adapter = new MyRecyclerAdapter(list);
				//非倾入性代码封装！----低耦合动画框架
				bouncingMenu= BouncingMenu.makeMenu(findViewById(R.id.rl), R.layout.layout_rv_sweet,adapter).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
