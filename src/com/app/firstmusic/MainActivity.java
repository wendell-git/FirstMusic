package com.app.firstmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.app.data.AppConstant;
import com.app.data.Mp3Info;
import com.app.service.PlayerService;
import com.app.tool.MediaUtil;


public class MainActivity extends Activity {

    private SimpleAdapter mAdapter;
    private ListView mMusiclist;
    private List<Mp3Info> mp3Infos;
    private HomeReceiver homeReceiver;  //自定义的广播接收器 
	private Button previousBtn;
	private Button repeatBtn;
	private Button playBtn;
	private Button shuffleBtn;
	private Button nextBtn;
	private TextView musicTitle;
	private TextView musicCurrent;
	private TextView musicDuration;
	private Button musicPlaying;
	private int listPosition;
  //一系列动作  
    public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION";  
    public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION";  
    public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT";  
    public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";  
    public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION";  
    public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";  
    
    private int repeatState;        //循环标识  
    private final int isCurrentRepeat = 1; // 单曲循环  
    private final int isAllRepeat = 2; // 全部循环  
    private final int isNoneRepeat = 3; // 无重复播放  
    private boolean isFirstTime = true;   
    private boolean isPlaying; // 正在播放  
    private boolean isPause; // 暂停  
    private boolean isNoneShuffle = true; // 顺序播放  
    private boolean isShuffle = false; // 随机播放  
    private int currentTime;  
    private int duration;  
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        mp3Infos = getMp3Infos();
        setListAdpter(mp3Infos);
        
        setViewOnclickListener();
        
        homeReceiver = new HomeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);
        registerReceiver(homeReceiver, filter);
        
    }

	public void initViews(){
		mMusiclist = (ListView) findViewById(R.id.music_list);
		mMusiclist.setOnItemClickListener(new MusicListItemClickListener());
		previousBtn = (Button) findViewById(R.id.previous_music);  
        repeatBtn = (Button) findViewById(R.id.repeat_music);  
        playBtn = (Button) findViewById(R.id.play_music);  
        shuffleBtn = (Button) findViewById(R.id.shuffle_music);  
        nextBtn = (Button) findViewById(R.id.next_music);  
        musicTitle = (TextView) findViewById(R.id.musicTitle);  
        musicCurrent = (TextView) findViewById(R.id.musicCurrent);  
        
	}
	 /** 
     * 给每一个按钮设置监听器 
     */  
    private void setViewOnclickListener() {  
        ViewOnClickListener viewOnClickListener = new ViewOnClickListener();  
        previousBtn.setOnClickListener(viewOnClickListener);  
        repeatBtn.setOnClickListener(viewOnClickListener);  
        playBtn.setOnClickListener(viewOnClickListener);  
        shuffleBtn.setOnClickListener(viewOnClickListener);  
        nextBtn.setOnClickListener(viewOnClickListener);  
        //musicPlaying.setOnClickListener(viewOnClickListener);  
    }  
    private class ViewOnClickListener implements OnClickListener {  
        Intent intent = new Intent();  
        @Override  
        public void onClick(View v) {  
        	System.out.println("view onclick listener");
            switch (v.getId()) {  
            case R.id.previous_music: // 上一首  
                playBtn.setBackgroundResource(R.drawable.pause_selector);  
                isFirstTime = false;  
                isPlaying = true;  
                isPause = false;  
                previous();  
                break;  
            case R.id.repeat_music: // 重复播放  
                if (repeatState == isNoneRepeat) {  
                    repeat_one();  
                    shuffleBtn.setClickable(false);  
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
                switch (repeatState) {  
                case isCurrentRepeat: // 单曲循环  
                    //repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);  
                    Toast.makeText(MainActivity.this, R.string.repeat_current,  
                            Toast.LENGTH_SHORT).show();  
                    break;  
                case isAllRepeat: // 全部循环  
                    //repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);  
                    Toast.makeText(MainActivity.this, R.string.repeat_all,  
                            Toast.LENGTH_SHORT).show();  
                    break;  
                case isNoneRepeat: // 无重复  
                    //repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);  
                    Toast.makeText(MainActivity.this, R.string.repeat_none,  
                            Toast.LENGTH_SHORT).show();  
                    break;  
                }  
  
                break;  
            case R.id.play_music: // 播放音乐  
                if(isFirstTime) {  
                    play();  
                    isFirstTime = false;  
                    isPlaying = true;  
                    isPause = false;  
                } else {  
                    if (isPlaying) {  
                        playBtn.setBackgroundResource(R.drawable.play_selector);  
                        intent.setClass(MainActivity.this, PlayerService.class);
                        //intent.setAction("com.wwj.media.MUSIC_SERVICE");  
                        intent.putExtra("MSG", AppConstant.PAUSE_MSG);  
                        startService(intent);  
                        isPlaying = false;  
                        isPause = true;  
                          
                    } else if (isPause) {  
                        playBtn.setBackgroundResource(R.drawable.pause_selector); 
                        intent.setClass(MainActivity.this, PlayerService.class);
                        //intent.setAction("com.wwj.media.MUSIC_SERVICE");  
                        intent.putExtra("MSG", AppConstant.CONTINUE_MSG);  
                        startService(intent);  
                        isPause = false;  
                        isPlaying = true;  
                    }  
                }  
                break;  
            case R.id.shuffle_music: // 随机播放  
//                if (isNoneShuffle) {  
//                    shuffleBtn  
//                            .setBackgroundResource(R.drawable.shuffle_selector);  
//                    Toast.makeText(HomeActivity.this, R.string.shuffle,  
//                            Toast.LENGTH_SHORT).show();  
//                    isNoneShuffle = false;  
//                    isShuffle = true;  
//                    shuffleMusic();  
//                    repeatBtn.setClickable(false);  
//                } else if (isShuffle) {  
//                    shuffleBtn  
//                            .setBackgroundResource(R.drawable.shuffle_none_selector);  
//                    Toast.makeText(HomeActivity.this, R.string.shuffle_none,  
//                            Toast.LENGTH_SHORT).show();  
//                    isShuffle = false;  
//                    isNoneShuffle = true;  
//                    repeatBtn.setClickable(true);  
//                }  
                break;  
            case R.id.next_music: // 下一首  
                playBtn.setBackgroundResource(R.drawable.pause_selector);  
                isFirstTime = false;  
                isPlaying = true;  
                isPause = false;  
                next();  
                break;  
//            case R.id.playing:  //正在播放  
//                Mp3Info mp3Info = mp3Infos.get(listPosition);  
//                Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);  
//                intent.putExtra("title", mp3Info.getTitle());     
//                intent.putExtra("url", mp3Info.getUrl());  
//                intent.putExtra("artist", mp3Info.getArtist());  
//                intent.putExtra("listPosition", listPosition);  
//                intent.putExtra("currentTime", currentTime);  
//                intent.putExtra("duration", duration);  
//                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAYING_MSG);  
//                startActivity(intent);  
//               break;  
            }  
        }  
    }  
	//自定义的BroadcastReceiver，负责监听从Service传回来的广播  
    public class HomeReceiver extends BroadcastReceiver {

    	@Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();   
            if(action.equals(MUSIC_CURRENT)){  
                //currentTime代表当前播放的时间  
                currentTime = intent.getIntExtra("currentTime", -1);  
                musicCurrent.setText(MediaUtil.formatTime(currentTime));  
            } else if (action.equals(MUSIC_DURATION)) {  
                duration = intent.getIntExtra("duration", -1);  
            }  
            else if(action.equals(UPDATE_ACTION)) {  
                //获取Intent中的current消息，current代表当前正在播放的歌曲  
                listPosition = intent.getIntExtra("current", -1);  
                if(listPosition >= 0) {  
                    musicTitle.setText(mp3Infos.get(listPosition).getTitle());  
                }  
            }else if(action.equals(REPEAT_ACTION)) {  
                repeatState = intent.getIntExtra("repeatState", -1);  
                switch (repeatState) {  
                case isCurrentRepeat: // 单曲循环  
                    //repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);  
                    shuffleBtn.setClickable(false);  
                    break;  
                case isAllRepeat: // 全部循环  
                    //repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);  
                    shuffleBtn.setClickable(false);  
                    break;  
                case isNoneRepeat: // 无重复  
                    repeatBtn  
                            .setBackgroundResource(R.drawable.repeat_none_selector);  
                    shuffleBtn.setClickable(true);  
                    break;  
                }  
            }  
            else if(action.equals(SHUFFLE_ACTION)) {  
                isShuffle = intent.getBooleanExtra("shuffleState", false);  
                if(isShuffle) {  
                    isNoneShuffle = false;  
                    //shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);  
                    repeatBtn.setClickable(false);  
                } else {  
                    isNoneShuffle = true;  
                    shuffleBtn.setBackgroundResource(R.drawable.shuffle_none_selector);  
                    repeatBtn.setClickable(true);  
                }  
            }  
        } 
    
    }
    private class MusicListItemClickListener implements OnItemClickListener {
    	@Override
    	public void onItemClick(AdapterView<?> parent, View view, int position,
    			long id) {
    		listPosition = position;  
            playMusic(listPosition); 
    	}
    }

    /**
    * 用于从数据库中查询歌曲的信息，保存在List当中
    *
    * @return
    */
    public List<Mp3Info> getMp3Infos() {
    	Cursor cursor = getContentResolver().query(
    		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
    		MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    	List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
    	for (int i = 0; i < cursor.getCount(); i++) {
    		Mp3Info mp3Info = new Mp3Info();
    		cursor.moveToNext();
    		long id = cursor.getLong(cursor
    			.getColumnIndex(MediaStore.Audio.Media._ID));	//音乐id
    		String title = cursor.getString((cursor	
    			.getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
    		String artist = cursor.getString(cursor
    			.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
    		long duration = cursor.getLong(cursor
    			.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
    		long size = cursor.getLong(cursor
    			.getColumnIndex(MediaStore.Audio.Media.SIZE));	//文件大小
    		String url = cursor.getString(cursor
    			.getColumnIndex(MediaStore.Audio.Media.DATA));				//文件路径
    	int isMusic = cursor.getInt(cursor
    		.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐
    	if (isMusic != 0) {		//只把音乐添加到集合当中
    		mp3Info.setId(id);
    		mp3Info.setTitle(title);
    		mp3Info.setArtist(artist);
    		mp3Info.setDuration(duration);
    		mp3Info.setSize(size);
    		mp3Info.setUrl(url);
    		mp3Infos.add(mp3Info);
    		}
    	}
    return mp3Infos;
    }
    /**
	 * 填充列表
	 * @param mp3Infos
	 */
	public void setListAdpter(List<Mp3Info> mp3Infos) {
		List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
		for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
			Mp3Info mp3Info = (Mp3Info) iterator.next();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("title", mp3Info.getTitle());
			map.put("Artist", mp3Info.getArtist());
			map.put("duration", MediaUtil.formatTime(mp3Info.getDuration()));
			map.put("size", String.valueOf(mp3Info.getSize()));
			map.put("url", mp3Info.getUrl());
			mp3list.add(map);
		}
		mAdapter = new SimpleAdapter(this, mp3list,
				R.layout.music_list_item_layout, new String[] { "title", "Artist", "duration" },
				new int[] { R.id.music_title, R.id.music_Artist, R.id.music_duration });
		mMusiclist.setAdapter(mAdapter);	
	}
	/** 
     * 下一首歌曲 
     */  
    public void next() {  
        listPosition = listPosition + 1;  
        if(listPosition <= mp3Infos.size() - 1) {  
            Mp3Info mp3Info = mp3Infos.get(listPosition);  
            musicTitle.setText(mp3Info.getTitle());  
            Intent intent = new Intent();  
            //intent.setAction("com.wwj.media.MUSIC_SERVICE");  
            intent.setClass(MainActivity.this, PlayerService.class);
            intent.putExtra("listPosition", listPosition);  
            intent.putExtra("url", mp3Info.getUrl());  
            intent.putExtra("MSG", AppConstant.NEXT_MSG);
            startService(intent);  
        } else {  
            Toast.makeText(MainActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();  
        }  
    }  
  
    /** 
     * 上一首歌曲 
     */  
    public void previous() {  
        listPosition = listPosition - 1;  
        if(listPosition >= 0) {  
            Mp3Info mp3Info = mp3Infos.get(listPosition);  
            musicTitle.setText(mp3Info.getTitle());  
            Intent intent = new Intent();  
            //intent.setAction("com.wwj.media.MUSIC_SERVICE");
            intent.setClass(MainActivity.this, PlayerService.class);
            intent.putExtra("listPosition", listPosition);  
            intent.putExtra("url", mp3Info.getUrl());  
            intent.putExtra("MSG", AppConstant.PRIVIOUS_MSG);  
            startService(intent);  
        }else {  
            Toast.makeText(MainActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();  
        }  
    }  
  
    public void play() {  
        playBtn.setBackgroundResource(R.drawable.pause_selector);  
        Mp3Info mp3Info = mp3Infos.get(listPosition);  
        musicTitle.setText(mp3Info.getTitle());  
        Intent intent = new Intent(); 
        intent.setClass(MainActivity.this, PlayerService.class);
        //intent.setAction("com.wwj.media.MUSIC_SERVICE");  
        intent.putExtra("listPosition", 0);  
        intent.putExtra("url", mp3Info.getUrl());  
        intent.putExtra("MSG", AppConstant.PLAY_MSG);  
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
     * 随机播放 
     */  
    public void shuffleMusic() {  
        Intent intent = new Intent(CTL_ACTION);  
        intent.putExtra("control", 4);  
        sendBroadcast(intent);  
    }  
    public void playMusic(int listPosition) {  
        if (mp3Infos != null) {  
            Mp3Info mp3Info = mp3Infos.get(listPosition);  
            musicTitle.setText(mp3Info.getTitle());  
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);  
            intent.putExtra("title", mp3Info.getTitle());     
            intent.putExtra("url", mp3Info.getUrl());  
            intent.putExtra("artist", mp3Info.getArtist());  
            intent.putExtra("listPosition", listPosition);  
            intent.putExtra("currentTime", currentTime);  
            intent.putExtra("repeatState", repeatState);  
            intent.putExtra("shuffleState", isShuffle);  
            intent.putExtra("MSG", AppConstant.PLAY_MSG);  
            startActivity(intent);  
        }  
    }  
      
    @Override  
    protected void onStop() {  
        // TODO Auto-generated method stub  
        super.onStop();  
    }  
      
      
    @Override  
    protected void onDestroy() {  
        // TODO Auto-generated method stub  
        super.onDestroy();  
    }  
      
    /** 
     * 按返回键弹出对话框确定退出 
     */  
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK  
                && event.getAction() == KeyEvent.ACTION_DOWN) {  
  
            new AlertDialog.Builder(this)  
                    .setIcon(R.drawable.ic_launcher)  
                    .setTitle("退出")  
                    .setMessage("您确定要退出？")  
                    .setNegativeButton("取消", null)  
                    .setPositiveButton("确定",  
                            new DialogInterface.OnClickListener() {  
  
                                @Override  
                                public void onClick(DialogInterface dialog,  
                                        int which) {  
                                    finish();  
                                    Intent intent = new Intent(  
                                            MainActivity.this,  
                                            PlayerService.class);  
                                    unregisterReceiver(homeReceiver);  
                                    stopService(intent); // 停止后台服务  
                                }  
                            }).show();  
  
        }  
        return super.onKeyDown(keyCode, event);  
    }  
      
}
