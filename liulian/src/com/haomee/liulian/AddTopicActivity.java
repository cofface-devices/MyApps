package com.haomee.liulian;
import java.io.File;
import java.util.Random;

import com.haomee.util.imageloader.ImageLoaderCharles;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haomee.consts.CommonConst;
import com.haomee.consts.PathConst;
import com.haomee.entity.ShareContent;
import com.haomee.entity.Topic;
import com.haomee.liulian.upyun.UpYunException;
import com.haomee.liulian.upyun.UpYunUtils;
import com.haomee.liulian.upyun.Uploader;
import com.haomee.util.FileDownloadUtil;
import com.haomee.util.NetworkUtil;
import com.haomee.view.LoadingDialog;
import com.haomee.view.MyToast;
import com.haomee.view.RoundCornerImageView;
import com.haomee.view.SelectPicPopupWindow;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sina.weibo.sdk.utils.LogUtil;

public class AddTopicActivity extends BaseActivity implements TextWatcher{
	private TextView topic_tip1, topic_tip2, topic_submit,tv_last;
	private RelativeLayout tip;
	private String rec_topic_id = "";
	private TextView tvBack,tvComit,tvAddImg;
	private EditText etTitle;
	private RoundCornerImageView iv_bg;
	private SelectPicPopupWindow menuWindow;
	private Activity activity_context;
	private File vFile;
	private String dir_temp;
	public static final int PHOTOHRAPH = 1;// 拍照
	public static final int PHOTOZOOM = 2; // 缩放
	public static final int PHOTORESOULT = 3;// 结果
	public static final int CROPIMAGES = 4;

	public static final int RESULTCODE=5;//回调
	private String path;
	private String top_bg = "";
	private LoadingDialog loadingDialog;
	private String picturePath;

	private InputMethodManager imm;
	private String title;//话题titile
	private Topic topic;//回调对象
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_topic);

		dir_temp = FileDownloadUtil.getDefaultLocalDir(PathConst.DIR_TEMP);		
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setFlag(true);
		activity_context=AddTopicActivity.this;
		initView();//初始化控件
	}
	/**
	 * 初始化控件
	 */
	private void initView() {

		tvComit=(TextView) findViewById(R.id.bt_comit);
		tvAddImg=(TextView) findViewById(R.id.imag_add);
		tv_last=(TextView) findViewById(R.id.tv_last);
		iv_bg=(RoundCornerImageView) findViewById(R.id.iv_bg);
		etTitle=(EditText) findViewById(R.id.deitText_title);
		tip=(RelativeLayout) findViewById(R.id.tip);

		// 随机背景颜色
		Random rdm = new Random();		
		iv_bg.setBgColor(CommonConst.bg_colors[rdm.nextInt(CommonConst.bg_colors.length)]);
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		//添加点击事件的监听
		tvAddImg.setOnClickListener(itemOnClick);
		tvComit.setOnClickListener(itemOnClick);
		etTitle.setOnClickListener(itemOnClick);
		//处理返回键
		findViewById(R.id.bt_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (imm.isActive(etTitle)) {
					imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
					etTitle.clearFocus();
					return;
				}
				// TODO Auto-generated method stub
				else {
					onBackPressed();
				}
			}
		});

		topic_tip1 = (TextView) findViewById(R.id.topic_tip1);
		topic_tip2 = (TextView) findViewById(R.id.topic_tip2);
		topic_tip2.setText(Html.fromHtml("<u>或去该话题发言</u>"));

		topic_tip2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(AddTopicActivity.this, TopicDetailActivity.class);
				intent.putExtra("topic_id", rec_topic_id);
				startActivity(intent);
			}
		});
		/**
		 * 通过焦点的来判断hiht的现实
		 */
		etTitle.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String hint = "";
				if (!hasFocus) {//失去焦点时现实提示信息
					hint = etTitle.getTag().toString();
					etTitle.setHint(hint);
				} else {//获得焦点时提示信息清空
					etTitle.setFocusable(true);
					hint = etTitle.getHint().toString();
					etTitle.setTag(hint);
					etTitle.setHint("");

				}				
			}
		});

		etTitle.addTextChangedListener(this);//文字改变的监听
	}

	/**
	 * 处理点击事件
	 */
	private OnClickListener itemOnClick = new OnClickListener() {


		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imag_add://更换图片
				menuWindow = new SelectPicPopupWindow(AddTopicActivity.this, itemOnClick);
				menuWindow.showAtLocation(AddTopicActivity.this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				// 隐藏软键盘
				if (imm.isActive(etTitle)) {
					imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
					etTitle.clearFocus();
					return;
				}
				break;
			case R.id.btn_take_photo://拍照
				menuWindow.dismiss();
				vFile = new File(dir_temp + "user_icon_temp.jpg");
				Uri uri = Uri.fromFile(vFile);
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(intent, PHOTOHRAPH);
				break;
			case R.id.btn_pick_photo://系统相册
				menuWindow.dismiss();
				selectPicFromLocal();//调用系统相册
				break;
			case R.id.bt_comit://提交数据
				if (!NetworkUtil.dataConnected(activity_context)) {
					MyToast.makeText(activity_context, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				if (etTitle.getText().toString().equals("")) {
					MyToast.makeText(activity_context, "请输入标题！", Toast.LENGTH_SHORT).show();
					tip.setVisibility(View.GONE);
					return;
				}
				@SuppressWarnings("deprecation")
				String url = PathConst.URL_PRIFERENCE_COMIT;
				LogUtil.e("添加话题", url + "");				
				RequestParams re = new RequestParams();

				title = etTitle.getText().toString().trim();

				re.put("accesskey", LiuLianApplication.current_user.getAccesskey());
				re.put("Luid", LiuLianApplication.current_user.getUid());
				re.put("title", title);				
				re.put("uid", LiuLianApplication.current_user.getUid());
				re.put("back_img", top_bg);
				AsyncHttpClient asyncHttp = new AsyncHttpClient();
				asyncHttp.get(url, re,new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String arg0) {
						super.onSuccess(arg0);
						try {
							if(arg0==null||arg0.length()==0){
								return ;
							}
							JSONObject json = new JSONObject(arg0);
							if(json==null||"".equals(json)){
								return;//防止网络连接超时出现空指针异常
							}
							if (1 == json.optInt("flag")) {//提交成功
								if (json.has("egg")) {
									JSONArray json_arr = json.getJSONArray("egg");
									JSONObject egg_obj = json_arr.getJSONObject(0);
									Intent intent_send = new Intent();
									intent_send.setClass(activity_context, ShareMedalActivity.class);
									ShareContent share = new ShareContent();
									share.setId(egg_obj.getString("id"));
									share.setTitle(egg_obj.getString("name"));
									share.setSummary(egg_obj.getString("desc"));
									share.setImg_url(egg_obj.getString("icon"));
									share.setRedirect_url(CommonConst.GOV_URL);
									intent_send.putExtra("share", share);
									activity_context.startActivity(intent_send);
								} else {
									MyToast.makeText(activity_context, json.optString("msg"), Toast.LENGTH_SHORT).show();
								}
								tip.setVisibility(View.GONE);
								activity_context.finish();
							} else if (2 == json.optInt("flag")) {//重复提交
								MyToast.makeText(activity_context, "提交未能成功", Toast.LENGTH_SHORT).show();
								//名字重复，显示推荐名字
								tip.setVisibility(View.VISIBLE);
								if (json.optInt("status") == 0) {
									rec_topic_id = json.optString("topic_id");
									if (rec_topic_id.equals("0")) {
										tip.setVisibility(View.GONE);
									} else {
										tip.setVisibility(View.VISIBLE);
									}
									topic_tip2.setVisibility(View.VISIBLE);
									topic_tip1.setText(json.optString("msg")+"，建议修改为" + json.optString("rec_name"));
								} else if (json.optInt("status") == 1) {
									topic_tip2.setVisibility(View.GONE);
									topic_tip1.setText("您发布的话题已存在或已删除，请与系统管理员联系！");
								} else if (json.optInt("status") == 2) {
									topic_tip2.setVisibility(View.GONE);
									topic_tip1.setText("您发布的话题已存在，目前正在审核中，请稍后到该话题内发布内容");
								} else {
									return;
								}

							} else if (0 == json.optInt("flag")) {//提交失败
								MyToast.makeText(activity_context, "返回结果为零", Toast.LENGTH_SHORT).show();
								MyToast.makeText(activity_context, json.optString("msg"), Toast.LENGTH_SHORT).show();
								tip.setVisibility(View.GONE);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				break;
			}
		}

	};
	
	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		this.startActivityForResult(intent, PHOTORESOULT);
	}

	/**
	 * 裁剪图片
	 */
	public void startCrop(String path) {
		Intent intent = new Intent();
		intent.putExtra("path", path);
		intent.putExtra("flag", false);
		intent.setClass(this, ImageCropActivity.class);
		startActivityForResult(intent, CROPIMAGES);
	}

	/**
	 * 获取回传数据进行相应的操作
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTOHRAPH) {//拍照后进行裁剪图片
			if (vFile != null && vFile.exists()) {
				startCrop(Uri.fromFile(vFile).getPath());
			}
		} else if (requestCode == CROPIMAGES) {//获取裁剪后的图片
			if (data != null) {				
				path = data.getStringExtra("path");
				loadingDialog.show();
				new ImageUploadTask().execute(path);
			}
		} else if(requestCode ==PHOTORESOULT){//打开系统相册进行裁剪图片
			if(data==null){//处理返回，取消键被点击报空指针异常
				return;
			}
			Uri startCrop = data.getData();
			if(startCrop!=null){
				findPicByUri(startCrop);
			}
		}
	}

	/**
	 * 根据图库图片uri获取图片
	 */
	private void findPicByUri(Uri selectedImage) {
		Cursor cursor=null;
		if(selectedImage!=null){
			cursor = getContentResolver().query(selectedImage, null, null, null, null);
		}
//		Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
		if (cursor != null) {//判断图片是否存在
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;
			if (picturePath == null || picturePath.equals("null")) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
		} else {//判断图片是否存在
			File file = new File(selectedImage.getPath());
			if (!file.exists()) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
		}
		startCrop(picturePath);//进行图片的裁剪
	}

	/**
	 * 检查加载背景图片
	 */
	class ImageUploadTask extends AsyncTask<String, Void, String> {
		private static final String TEST_API_KEY = "yuIOo0F9DDf8ZbkZa1syRG/zdes="; // 测试使用的表单api验证密钥
		private static final String BUCKET = "haomee"; // 存储空间
		private final long EXPIRATION = System.currentTimeMillis() / 1000 + 1000 * 5 * 10; //过期时间，必须大于当前时间
		String SAVE_KEY = File.separator + "haomee" + File.separator + System.currentTimeMillis() + ".jpg";
		@Override
		protected String doInBackground(String... arg0) {
			String string = null;
			try {
				String policy = UpYunUtils.makePolicy(SAVE_KEY, EXPIRATION, BUCKET);
				String signature = UpYunUtils.signature(policy + "&" + TEST_API_KEY);
				string = Uploader.upload(policy, signature, BUCKET, arg0[0]);
			} catch (UpYunException e) {
				e.printStackTrace();
			}
			return string;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {				
				top_bg = "http://haomee.b0.upaiyun.com" + SAVE_KEY;

                ImageLoaderCharles.getInstance(AddTopicActivity.this).addTask(top_bg,iv_bg);
			} else {
				top_bg = null;
				Toast.makeText(activity_context, "当前网络不可用,无法进行此操作!!", Toast.LENGTH_LONG).show();
			}
			loadingDialog.dismiss();
			loadingDialog.setFlag(false);
		}

	}

	/**
	 * 监听edittext的改变
	 */
	@Override  
	public void afterTextChanged(Editable s) {  
//		Log.d("----tag", "afterTextChanged"); 
	}  
	//edittext编辑之前调用
	@Override  
	public void beforeTextChanged(CharSequence s, int start, int count,  
			int after) {  
//		Log.d("----tag", "beforeTextChanged:" + s + "-" + start + "-" + count + "-" + after);  
	}  
	//edittext被编辑之后调用
	@Override  
	public void onTextChanged(CharSequence s, int start, int before,  
			int count) {  
//		Log.d("----tag", "onTextChanged:" + s + "-" + "-" + start + "-" + before + "-" + count);  
		if("".equals(s.toString())){
			tip.setVisibility(View.GONE);
		}	
		int length = 24-etTitle.getText().length();
		if(length>=0){
			if(length>9){
				tv_last.setText("(还能输入"+length+"个字)");
			}else{
				tv_last.setText("(还能输入"+"0"+length+"个字)");
			} 	
		}
	}  
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		loadingDialog.setFlag(false);
	}
}
