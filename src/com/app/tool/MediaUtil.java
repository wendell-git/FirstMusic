package com.app.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.app.data.AlbumInfo;
import com.app.data.ArtistInfo;
import com.app.data.FolderInfo;
import com.app.data.Mp3Info;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files.FileColumns;


public class MediaUtil {
	/**
	 * 用于从数据库中查询歌曲的信息，保存在List当中
	 * 
	 * @return
	 */
	public static List<Mp3Info> getMp3Infos(Context context) {
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			Mp3Info mp3Info = new Mp3Info();
			long id = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media._ID));				//音乐id
			String title = cursor.getString((cursor	
					.getColumnIndex(MediaStore.Audio.Media.TITLE)));			//音乐标题
			String artist = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));			//艺术家
			long duration = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));			//时长
			long size = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE));				//文件大小
			String url = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));				//文件路径
			int isMusic = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));			//是否为音乐
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
	 * 获取专辑信息
	 * @param context
	 * @return
	 */
	public static List<AlbumInfo> queryAlbums(Context context) {
//		if(mAlbumInfoDao == null) {
//			mAlbumInfoDao = new AlbumInfoDao(context);
//		}
//		
//		SPStorage sp = new SPStorage(context);
//		
		Uri uri = Albums.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		StringBuilder where = new StringBuilder(Albums._ID
				+ " in (select distinct " + Media.ALBUM_ID
				+ " from audio_meta where (1=1 ");
		
//		if(sp.getFilterSize()) {
//			where.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
//		}
//		if(sp.getFilterTime()) {
//			where.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
//		}
//		where.append("))");

//		if (mAlbumInfoDao.hasData()) {
//			return mAlbumInfoDao.getAlbumInfo();
//		} else {
//			// Media.ALBUM_KEY 按专辑名称排序
//			List<AlbumInfo> list = getAlbumList(cr.query(uri, proj_album,
//					where.toString(), null, Media.ALBUM_KEY));
//			mAlbumInfoDao.saveAlbumInfo(list);
//			return list;
//		}
		List<AlbumInfo> list = getAlbumList(cr.query(uri, null,
				null, null, Media.ALBUM_KEY));
		return list;
	}
	/**
	 * 往List集合中添加Map对象数据，每一个Map对象存放一首音乐的所有属性
	 * @param mp3Infos
	 * @return
	 */
	public static List<HashMap<String, String>> getMusicMaps(
			List<Mp3Info> mp3Infos) {
		List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
		for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
			Mp3Info mp3Info = (Mp3Info) iterator.next();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("title", mp3Info.getTitle());
			map.put("Artist", mp3Info.getArtist());
			map.put("duration", formatTime(mp3Info.getDuration()));
			map.put("size", String.valueOf(mp3Info.getSize()));
			map.put("url", mp3Info.getUrl());
			mp3list.add(map);
		}
		return mp3list;
	}
	
	/**
	 * 获取包含音频文件的文件夹信息
	 * @param context
	 * @return
	 */
	public static List<FolderInfo> queryFolder(Context context) {
//		if(mFolderInfoDao == null) {
//			mFolderInfoDao = new FolderInfoDao(context);
//		}
//		SPStorage sp = new SPStorage(context);
		Uri uri = MediaStore.Files.getContentUri("external");
		ContentResolver cr = context.getContentResolver();
//		StringBuilder mSelection = new StringBuilder(FileColumns.MEDIA_TYPE
//				+ " = " + FileColumns.MEDIA_TYPE_AUDIO + " and " + "("
//				+ FileColumns.DATA + " like'%.mp3' or " + Media.DATA
//				+ " like'%.wma')");
//		// 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
//		if(sp.getFilterSize()) {
//			mSelection.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
//		}
//		if(sp.getFilterTime()) {
//			mSelection.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
//		}
//		mSelection.append(") group by ( " + FileColumns.PARENT);
//		if (mFolderInfoDao.hasData()) {
//			return mFolderInfoDao.getFolderInfo();
//		} else {
//			List<FolderInfo> list = getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null, null));
//			mFolderInfoDao.saveFolderInfo(list);
//			return list;
//		}
		List<FolderInfo> list = getFolderList(cr.query(uri, null, null, null, null));
		return list;
	}

	/**
	 * 获取歌手信息
	 * @param context
	 * @return
	 */
	public static List<ArtistInfo> queryArtist(Context context) {
//		if(mArtistInfoDao == null) {
//			mArtistInfoDao = new ArtistInfoDao(context);
//		}
		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
//		if (mArtistInfoDao.hasData()) {
//			return mArtistInfoDao.getArtistInfo();
//		} else {
//			List<ArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
//					null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
//							+ " desc"));
//			mArtistInfoDao.saveArtistInfo(list);
//			return list;
//		}
		List<ArtistInfo> list = getArtistList(cr.query(uri, null,
				null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
						+ " desc"));
		return list;
	}
	public static List<AlbumInfo> getAlbumList(Cursor cursor) {
		List<AlbumInfo> list = new ArrayList<AlbumInfo>();
		while (cursor.moveToNext()) {
			AlbumInfo info = new AlbumInfo();
			info.album_name = cursor.getString(cursor
					.getColumnIndex(Albums.ALBUM));
			info.album_id = cursor.getInt(cursor.getColumnIndex(Albums._ID));
			info.number_of_songs = cursor.getInt(cursor
					.getColumnIndex(Albums.NUMBER_OF_SONGS));
			info.album_art = cursor.getString(cursor
					.getColumnIndex(Albums.ALBUM_ART));
			System.out.println("AlbumInfo="+info.album_name);
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static List<ArtistInfo> getArtistList(Cursor cursor) {
		List<ArtistInfo> list = new ArrayList<ArtistInfo>();
		while (cursor.moveToNext()) {
			ArtistInfo info = new ArtistInfo();
			info.artist_name = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
			info.number_of_tracks = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static List<FolderInfo> getFolderList(Cursor cursor) {
		List<FolderInfo> list = new ArrayList<FolderInfo>();
		while (cursor.moveToNext()) {
			FolderInfo info = new FolderInfo();
			String filePath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Files.FileColumns.DATA));
			info.folder_path = filePath.substring(0,
					filePath.lastIndexOf(File.separator));
			info.folder_name = info.folder_path.substring(info.folder_path
					.lastIndexOf(File.separator) + 1);
			list.add(info);
		}
		cursor.close();
		return list;
	}

	
	
	/**
	 * 格式化时间，将毫秒转换为分:秒格式
	 * @param time
	 * @return
	 */
	public static String formatTime(long time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}
		return min + ":" + sec.trim().substring(0, 2);
	}
}
