package com.app.firstmusic;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.app.data.AppConstant;
import com.app.data.Mp3Info;
import com.app.service.PlayerService;
import com.app.tool.MediaUtil;


/**
 * 播放音乐界面
 * @author wwj
 * 从主界面传递过来歌曲的Id、歌曲名、歌手、歌曲路径、播放状态
 */
public class PlayerActivity extends Activity{
	private TextView musicTitle = null;
	private TextView musicArtist = null;
	private Button previousBtn; // 上一首
	private Button repeatBtn; 	// 重复（单曲循环、全部循环）
	private Button playBtn;	 	// 播放（播放、暂停）
	private Button shuffleBtn; // 随机播放
	private Button nextBtn; 	// 下一首
	private Button searchBtn;	//查找歌曲
	private Button queueBtn;	//歌曲列表
	private SeekBar music_progressBar; 	//歌曲进度
	private TextView currentProgress;	//当前进度消耗的时间
	private TextView finalProgress;		//歌曲时间
	
	
	private String title;		//歌曲标题
	private String artist;		//歌曲艺术家
	private String url;			//歌曲路径
	private int listPosition;	//播放歌曲在mp3Infos的位置
	private int currentTime;	//当前歌曲播放时间
	private int duration;		//歌曲长度
	private int flag;			//播放标识
	
	private int repeatState;
	private final int isCurrentRepeat = 1; // 单曲循环
	private final int isAllRepeat = 2; 		// 全部循环
	private final int isNoneRepeat = 3; 	// 无重复播放
	private boolean isPlaying; 				// 正在播放
	private boolean isPause; 				// 暂停
	private boolean isNoneShuffle;			 // 顺序播放
	private boolean isShuffle; 			// 随机播放
	
	private List<Mp3Info> mp3Infos;
	
	
	private PlayerReceiver playerReceiver;	
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION";	//更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION";		//控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT";	//音乐当前时间改变动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";//音乐播放长度改变动作
	public static final String MUSIC_PLAYING = "com.wwj.action.MUSIC_PLAYING";	//音乐正在播放动作
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION";	//音乐重复播放动作
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";//音乐随机播放动作
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity_layout);
		musicTitle = (TextView) findViewById(R.id.musicTitle);
		musicArtist = (TextView) findViewById(R.id.musicArtist);
		
		findViewById();
		setViewOnclickListener();
		mp3Infos = MediaUtil.getMp3Infos(PlayerActivity.this);
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		registerReceiver(playerReceiver, filter);
		
	}
	
	/**
	 * 从界面上根据id获取按钮
	 */
	private void findViewById() {
		previousBtn = (Button) findViewById(R.id.previous_music);
		repeatBtn = (Button) findViewById(R.id.repeat_music);
		playBtn = (Button) findViewById(R.id.play_music);
		shuffleBtn = (Button) findViewById(R.id.shuffle_music);
		nextBtn = (Button) findViewById(R.id.next_music);
		searchBtn = (Button) findViewById(R.id.search_music);
		queueBtn = (Button) findViewById(R.id.play_queue);
		music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
		currentProgress = (TextView) findViewById(R.id.current_progress);
		finalProgress = (TextView) findViewById(R.id.final_progress);
	}
	
	
	/**
	 * 给每一个按钮设置监听器
	 */
	private void setViewOnclickListener() {
		ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();
		previousBtn.setOnClickListener(ViewOnClickListener);
		repeatBtn.setOnClickListener(ViewOnClickListener);
		playBtn.setOnClickListener(ViewOnClickListener);
		shuffleBtn.setOnClickListener(ViewOnClickListener);
		nextBtn.setOnClickListener(ViewOnClickListener);
		searchBtn.setOnClickListener(ViewOnClickListener);
		queueBtn.setOnClickListener(ViewOnClickListener);
		music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
	}
	
	/**
	 * 在OnResume中初始化和接收Activity数据
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		title = bundle.getString("title");
		artist = bundle.getString("artist");
		url = bundle.getString("url");
		listPosition = bundle.getInt("listPosition");
		repeatState = bundle.getInt("repeatState");
		isShuffle = bundle.getBoolean("shuffleState");
		flag = bundle.getInt("MSG");
		currentTime = bundle.getInt("currentTime");
		duration = bundle.getInt("duration");
		initView();
	}
	
	/**
	 * 初始化界面
	 */
	public void initView() {
		musicTitle.setText(title);
		musicArtist.setText(artist);
		music_progressBar.setProgress(currentTime);
		music_progressBar.setMax(duration);
		switch (repeatState) {
		case isCurrentRepeat: // 单曲循环
			shuffleBtn.setClickable(false);
			//repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);
			break;
		case isAllRepeat: // 全部循环
			shuffleBtn.setClickable(false);
			//repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);
			break;
		case isNoneRepeat: // 无重复
			shuffleBtn.setClickable(true);
			repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);
			break;
		}
		if(isShuffle) {
			isNoneShuffle = false;
			//shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);
			repeatBtn.setClickable(false);
		} else {
			isNoneShuffle = true;
			shuffleBtn.setBackgroundResource(R.drawable.shuffle_none_selector);
			repeatBtn.setClickable(true);
		}
		if(flag == AppConstant.PLAYING_MSG) {	//如果播放信息是正在播放
			Toast.makeText(PlayerActivity.this, "正在播放--" + title, 1).show();
		}
		else if(flag == AppConstant.PLAY_MSG) { //如果是点击列表播放歌曲的话
			play();
		}
		playBtn.setBackgroundResource(R.drawable.pause_selector);
		isPlaying = true;
		isPause = false;
	}

	/**
	 * 反注册广播
	 */
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(playerReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	/**
	 * 控件点击事件
	 * @author wwj
	 *
	 */
	private class ViewOnclickListener implements OnClickListener {
		Intent intent = new Intent();
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.play_music:
				if (isPlaying) {
					playBtn.setBackgroundResource(R.drawable.pause_selector);
					intent.setClass(PlayerActivity.this, PlayerService.class);
					intent.putExtra("MSG", AppConstant.PAUSE_MSG);
					startService(intent);
					isPlaying = false;
					isPause = true;
					
				} else if (isPause) {
					playBtn.setBackgroundResource(R.drawable.play_selector);
					intent.setClass(PlayerActivity.this, PlayerService.class);
					intent.putExtra("MSG", AppConstant.CONTINUE_MSG);
					startService(intent);
					isPause = false;
					isPlaying = true;
				}
				break;
			case R.id.previous_music:		//上一首歌曲
				previous_music();
				break;
			case R.id.next_music:			//下一首歌曲
				next_music();
				break;
			case R.id.repeat_music:			//重复播放音乐
				if (repeatState == isNoneRepeat) {
					repeat_one();
					shuffleBtn.setClickable(false);	//是随机播放变为不可点击状态
					repeatState = isCurrentRepeat;	
				} else if (repeatState == isCurrentRepeat) {
					repeat_all();
					shuffleBtn.setClickable(false);
					repeatState = isAllRepeat;
				} else if (repeatState == isAllRepeat) {
					repeat_none();
					shuffleBtn.setClickable(true);
					repeatState = isNoneRepeat;
				}
				Intent intent = new Intent(REPEAT_ACTION);
				switch (repeatState) {
				case isCurrentRepeat: // 单曲循环
					//repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);
					Toast.makeText(PlayerActivity.this, R.string.repeat_current,
							Toast.LENGTH_SHORT).show();
					
					
					intent.putExtra("repeatState", isCurrentRepeat);
					sendBroadcast(intent);
					break;
				case isAllRepeat: // 全部循环
					//repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);
					Toast.makeText(PlayerActivity.this, R.string.repeat_all,
							Toast.LENGTH_SHORT).show();
					intent.putExtra("repeatState", isAllRepeat);
					sendBroadcast(intent);
					break;
				case isNoneRepeat: // 无重复
					repeatBtn
							.setBackgroundResource(R.drawable.repeat_none_selector);
					Toast.makeText(PlayerActivity.this, R.string.repeat_none,
							Toast.LENGTH_SHORT).show();
					intent.putExtra("repeatState", isNoneRepeat);
					break;
				}
				break;
			case R.id.shuffle_music:			//随机播放状态
				Intent shuffleIntent = new Intent(SHUFFLE_ACTION);
				if (isNoneShuffle) {			//如果当前状态为非随机播放，点击按钮之后改变状态为随机播放
//					shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);
					Toast.makeText(PlayerActivity.this, R.string.shuffle,
							Toast.LENGTH_SHORT).show();
					isNoneShuffle = false;
					isShuffle = true;
					shuffleMusic();
					repeatBtn.setClickable(false);
					shuffleIntent.putExtra("shuffleState", true);
					sendBroadcast(shuffleIntent);
				} else if (isShuffle) {
					shuffleBtn
							.setBackgroundResource(R.drawable.shuffle_none_selector);
					Toast.makeText(PlayerActivity.this, R.string.shuffle_none,
							Toast.LENGTH_SHORT).show();
					isShuffle = false;
					isNoneShuffle = true;
					repeatBtn.setClickable(true);
					shuffleIntent.putExtra("shuffleState", false);
					sendBroadcast(shuffleIntent);
				}
				break;
			}
		}
	}
	
	/**
	 * 实现监听Seekbar的类
	 * @author wwj
	 *
	 */
	private class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(fromUser) {
				audioTrackChange(progress);	//用户控制进度的改变
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
		
	}
	
	/**
	 * 播放音乐
	 */
	public void play() {
		//开始播放的时候为顺序播放
		repeat_none();
		Intent intent = new Intent();
		intent.setClass(PlayerActivity.this, PlayerService.class);
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", flag);
		startService(intent);
	}


	/**
	 * 随机播放
	 */
	public void shuffleMusic() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 4);
		sendBroadcast(intent);
	}

	public void audioTrackChange(int progress) {
		Intent intent = new Intent();
		intent.setClass(PlayerActivity.this, PlayerService.class);
		intent.putExtra("url", url);
		intent.putExtra("listPosition", listPosition);
		if(isPause) {
			intent.putExtra("MSG", AppConstant.PAUSE_MSG);
		}
		else {
			intent.putExtra("MSG", AppConstant.PROGRESS_CHANGE);
		}
		intent.putExtra("progress", progress);
		startService(intent);
	}

	/**
	 * 单曲循环
	 */
	public void repeat_one() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	
	/**
	 * 全部循环
	 */
	public void repeat_all() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 2);
		sendBroadcast(intent);
	}

	/**
	 * 顺序播放
	 */
	public void repeat_none() {
		Intent intent = new Intent(CTL_ACTION);
		intent.putExtra("control", 3);
		sendBroadcast(intent);
	}

	/**
	 * 上一首
	 */
	public void previous_music() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		listPosition = listPosition - 1;
		if(listPosition >= 0) {
			Mp3Info mp3Info = mp3Infos.get(listPosition); 	//上一首MP3
			musicTitle.setText(mp3Info.getTitle());
			musicArtist.setText(mp3Info.getArtist());
			url = mp3Info.getUrl();
			Intent intent = new Intent();
			intent.setClass(PlayerActivity.this, PlayerService.class);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("MSG", AppConstant.PRIVIOUS_MSG);
			startService(intent);
		}
		else {
			Toast.makeText(PlayerActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 下一首
	 */
	public void next_music() {
		playBtn.setBackgroundResource(R.drawable.play_selector);
		listPosition = listPosition + 1;
		if(listPosition <= mp3Infos.size() - 1) {
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			url = mp3Info.getUrl();
			musicTitle.setText(mp3Info.getTitle());
			musicArtist.setText(mp3Info.getArtist());
			Intent intent = new Intent();
			intent.setClass(PlayerActivity.this, PlayerService.class);
			intent.putExtra("url", mp3Info.getUrl());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("MSG", AppConstant.NEXT_MSG);
			startService(intent);
		} else {
			Toast.makeText(PlayerActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * 用来接收从service传回来的广播的内部类
	 * @author wwj
	 *
	 */
	public class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				currentProgress.setText(MediaUtil.formatTime(currentTime));
				music_progressBar.setProgress(currentTime);
			} else if(action.equals(MUSIC_DURATION)) {
				int duration = intent.getIntExtra("duration", -1);
				music_progressBar.setMax(duration);
				finalProgress.setText(MediaUtil.formatTime(duration));
			} else if(action.equals(UPDATE_ACTION)) {
				//获取Intent中的current消息，current代表当前正在播放的歌曲
				listPosition = intent.getIntExtra("current", -1);
				url = mp3Infos.get(listPosition).getUrl();
				if(listPosition >= 0) {
					musicTitle.setText(mp3Infos.get(listPosition).getTitle());
					musicArtist.setText(mp3Infos.get(listPosition).getArtist());
				}
				if(listPosition == 0) {
					finalProgress.setText(MediaUtil.formatTime(mp3Infos.get(listPosition).getDuration()));
					playBtn.setBackgroundResource(R.drawable.pause_selector);
					isPause = true;
				}
			}
		}
		
	}
	
}
