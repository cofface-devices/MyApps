package com.haomee.liulian;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.haomee.consts.CommonConst;
import com.haomee.consts.PathConst;
import com.haomee.entity.TextItem;
import com.haomee.entity.UserTextList;
import com.haomee.util.NetworkUtil;
import com.haomee.util.ViewUtil;
import com.haomee.view.LoadingDialog;
import com.haomee.view.MyToast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestHuiGuActivity extends BaseActivity {
	private android.widget.LinearLayout.LayoutParams layoutParams; 
	private Activity activity_context;
	private LinearLayout ll_content;
	private LinearLayout ll_por_text,ll_next_text;
	private TextView total_pager;
	private ArrayList<TextItem> text_list;
	private int screen_width;//屏幕的宽度

	private int current_pag=1;
	private int total_pag=0;
	private LayoutInflater inflater;
	private View text_view;
	private TextView left_title,right_title;
//	private TextView left_number,right_number;
	private TextView current_finish;
	private ViewGroup.LayoutParams params_left;
	private ViewGroup.LayoutParams params_right;
	private LoadingDialog loadingDialog;
	private LinearLayout ll_left_background;
	private LinearLayout ll_right_background;

	private TranslateAnimation transAnimation_por;//平移动画
	private TranslateAnimation transAnimation_next;//平移动画
	private Random rdm;
	
	private View left_view,left_equal_view,left_small,left_equal,left_big;
	private TextView left_person_number_left;
	private View right_view,right_equal_view,right_small,right_equal,right_big;
	private TextView right_person_number_right;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_huigu);
		activity_context=this;
		loadingDialog = new LoadingDialog(this);
		inflater=LayoutInflater.from(this);
		screen_width=ViewUtil.getScreenWidth(activity_context);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		transAnimation_por = new TranslateAnimation(0, 1000, 0, 0);
		transAnimation_por.setDuration(500);

		transAnimation_next = new TranslateAnimation(0, -1000, 0, 0);
		transAnimation_next.setDuration(500);
		rdm = new Random();
		//返回键
		findViewById(R.id.bt_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent("MyReceiver_Action");
				// 可通过Intent携带消息
				activity_context.sendBroadcast(intent);
			}
		});
		getTextResult ();
		initView();

	}

	private void initView() {
		ll_content=(LinearLayout) findViewById(R.id.ll_content);
		total_pager=(TextView) findViewById(R.id.total_page);
		ll_por_text=(LinearLayout) findViewById(R.id.linearLayout_text_por);
		ll_next_text=(LinearLayout) findViewById(R.id.linearLayout_text_next);

		ll_por_text.setOnClickListener(new OnClickListener() {//上一题

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(text_list==null&&text_list.size()==0){
					return;
				}
				if(current_pag==1){//第一页
					MyToast.makeText(activity_context, "已是第一题了", Toast.LENGTH_SHORT).show();
				}else {
					ll_por_text.setClickable(false);
					ll_next_text.setClickable(false);
					current_pag--;
					total_pager.setText(current_pag+"/"+total_pag);
					text_view.startAnimation(transAnimation_por);
					ll_content.removeView(text_view);
					addView(current_pag-1);
				}

			}
		});
		ll_next_text.setOnClickListener(new OnClickListener() {//下一题

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(text_list==null&&text_list.size()==0){
					return;
				}
				if(current_pag==total_pag){
					MyToast.makeText(activity_context, "已是最后一题了", Toast.LENGTH_SHORT).show();
				}else {
					ll_por_text.setClickable(false);
					ll_next_text.setClickable(false);
					current_pag++;
					total_pager.setText(current_pag+"/"+total_pag);
					text_view.startAnimation(transAnimation_next);
					ll_content.removeView(text_view);
					addView(current_pag-1);
				}

			}
		});
		/**
		 * 动画完成让按钮可点击
		 */
		transAnimation_por.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				ll_por_text.setClickable(true);
				ll_next_text.setClickable(true);
			}
		});

		transAnimation_next.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				ll_por_text.setClickable(true);
				ll_next_text.setClickable(true);
			}
		});
		//		addView(0);
	}

	/**
	 * 添加上一题或下一题
	 */
	private void addView( int index) {
		text_view = inflater.inflate(R.layout.activity_user_text, null);
		ll_content.addView(text_view, layoutParams);
		// 随机背景颜色
		ll_left_background=(LinearLayout) text_view.findViewById(R.id.ll_left);
		ll_right_background=(LinearLayout) text_view.findViewById(R.id.ll_right);

		int int_left = rdm.nextInt(CommonConst.bg_colors.length);
		int int_right = rdm.nextInt(CommonConst.bg_colors.length);
		while(int_left==int_right){
			int_right = rdm.nextInt(CommonConst.bg_colors.length);
		}
		ll_left_background.setBackgroundColor(CommonConst.bg_colors[int_left]);
		ll_right_background.setBackgroundColor(CommonConst.bg_colors[int_right]);

		
		left_equal_view=text_view.findViewById(R.id.left_view_equal);
		left_view=text_view.findViewById(R.id.left_view);
		left_small=text_view.findViewById(R.id.left_small);
		left_equal=text_view.findViewById(R.id.left_equal);
		left_big=text_view.findViewById(R.id.left_big);
		left_person_number_left=(TextView) text_view.findViewById(R.id.left_person_number);
		
		right_equal_view=text_view.findViewById(R.id.right_equal_view);
		right_view=text_view.findViewById(R.id.right_view);
		right_small=text_view.findViewById(R.id.right_small);
		right_equal=text_view.findViewById(R.id.right_equal);
		right_big=text_view.findViewById(R.id.right_big);
		right_person_number_right=(TextView) text_view.findViewById(R.id.right_person_number);
		
		left_person_number_left.setVisibility(View.VISIBLE);
		right_person_number_right.setVisibility(View.VISIBLE);
		
		left_title=(TextView) text_view.findViewById(R.id.text_left);
		right_title=(TextView) text_view.findViewById(R.id.text_right);
		current_finish=(TextView) text_view.findViewById(R.id.current_finish);
		current_finish.setVisibility(View.GONE);
		if(text_list!=null){
			final TextItem item = text_list.get(index);
			final Intent intent_filter=new Intent();
			intent_filter.setClass(activity_context, FilterActivity.class);
			intent_filter.putExtra("qid", item.getId());
			intent_filter.putExtra("flag", 3);
			left_person_number_left.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					intent_filter.putExtra("item_id", item.getLeft_id());
					activity_context.startActivity(intent_filter);
				}
			});
			right_person_number_right.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					intent_filter.putExtra("item_id", item.getRight_id());
					activity_context.startActivity(intent_filter);
				}
			});


			left_person_number_left.setText(item.getLeft_num());
			right_person_number_right.setText(item.getRight_num());
			
			left_title.setText(item.getLeft_title());
			right_title.setText(item.getRight_title());

			final int left_peolpe_number=Integer.parseInt(item.getLeft_num());
			final int right_peolpe_number=Integer.parseInt(item.getRight_num());

			params_left = left_person_number_left.getLayoutParams();
			params_right = right_person_number_right.getLayoutParams();

			
			
			int left_weight=0;
			int right_weight=0;
			if(left_peolpe_number>right_peolpe_number){//左边人数多
				left_weight=2;
				right_weight=3;
				left_big.setVisibility(View.VISIBLE);
				right_small.setVisibility(View.VISIBLE);
				
				
				params_left.width=screen_width/left_weight+screen_width/left_weight/6;
				params_left.height=screen_width/left_weight+screen_width/left_weight/6;
				params_right.width=screen_width/right_weight+screen_width/right_weight/4;
				params_right.height=screen_width/right_weight+screen_width/right_weight/4;
				left_view.setVisibility(View.GONE);
				right_view.setVisibility(View.VISIBLE);
			}else if(left_peolpe_number<right_peolpe_number){//右边人数多
				left_weight=3;
				right_weight=2;
				
				left_view.setVisibility(View.VISIBLE);
				left_small.setVisibility(View.VISIBLE);
				right_big.setVisibility(View.VISIBLE);
				
				params_left.width=screen_width/left_weight+screen_width/left_weight/4;
				params_left.height=screen_width/left_weight+screen_width/left_weight/4;
				params_right.width=screen_width/right_weight+screen_width/right_weight/6;
				params_right.height=screen_width/right_weight+screen_width/right_weight/6;
				
			}else {//相同
				left_weight=2;
				right_weight=2;
				
				left_equal.setVisibility(View.VISIBLE);
				right_equal.setVisibility(View.VISIBLE);
				
				params_left.width=screen_width/left_weight;
				params_left.height=screen_width/left_weight;
				params_right.width=screen_width/right_weight;
				params_right.height=screen_width/right_weight;
				
				left_equal_view.setVisibility(View.VISIBLE);
				right_equal_view.setVisibility(View.VISIBLE);
			}
			
			left_person_number_left.setLayoutParams(params_left);
			right_person_number_right.setLayoutParams(params_right);
		}


	}



	/**
	 * 获取答过的题
	 */
	private void getTextResult (){
		loadingDialog.show();
		if (!NetworkUtil.dataConnected(activity_context)) {
			loadingDialog.dismiss();
			MyToast.makeText(activity_context, activity_context.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
			return ;
		}
		String url = PathConst.URL_LEVER_TEXT;
		RequestParams rp=new RequestParams();
		rp.put("Luid",LiuLianApplication.current_user.getUid());
		AsyncHttpClient asyncHttp = new AsyncHttpClient();
		asyncHttp.get(url,rp, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String arg0) {
				// TODO Auto-generated method stub
				super.onSuccess(arg0);
				try {
					if(arg0==null||arg0.length()==0){
						loadingDialog.dismiss();
						return ;
					}
					JSONObject json = new JSONObject(arg0);
					if(json==null||"".equals(json)){
						loadingDialog.dismiss();
						return ;//防止网络连接超时出现空指针异常
					}
					text_list=new ArrayList<TextItem>();
					JSONArray array=json.getJSONArray("list");
					if(array==null||array.length()==0){
						loadingDialog.dismiss();
						return;
					}
					for(int index=0;index<array.length();index++){
						JSONObject obj=array.getJSONObject(index);
						if(!obj.optBoolean("is_answer")){
							continue;
						}
						TextItem item=new TextItem();
						item.setId(obj.optString("id"));
						item.setLeft_id(obj.optString("left_id"));
						item.setRight_id(obj.optString("right_id"));
						item.setLeft_title(obj.optString("left_title"));
						item.setRight_title(obj.optString("right_title"));
						item.setLeft_num(obj.optString("left_num"));
						item.setRight_num(obj.optString("right_num"));
						text_list.add(item);
					}
					total_pag=text_list.size();
					total_pager.setText("1/"+total_pag);
					loadingDialog.dismiss();
					addView(0);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					loadingDialog.dismiss();
					e.printStackTrace();
				}

			}
		});
	}
	/*
	 * 处理返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent("MyReceiver_Action");
			// 可通过Intent携带消息
			activity_context.sendBroadcast(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
