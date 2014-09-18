package com.nami.android.sinema;

import java.io.File;

import android.os.Environment;

public final class SinemaConstant {
	public static final double VERSION = 1.7;
	public static final String APPNAME = "Sinema";
	public static final String MOVIE_LIST_URL = "http://yts.re/api/list.xml";
	public static final String MOVIE_DETAIL_URL = "http://yts.re/api/movie.xml";
	public static final String QUALITY = "720p";
	public static final String RATING = "0";
	public static final String CLIENT = "remote";
	public static final String GENRE = "All";
	public static final String LIMIT = "50";
	public static final String SORT = "Seeds";
	public static final String ORDER = "desc";
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"Download"+ File.separator;//"/sdcard/Download/";
	public static final String RETURN_MESSAGE = "Press the back button to return to "+APPNAME;
	public static final String VERSION_URL = "https://github.com/ShahNami/"+APPNAME+"/tags";
	public static final String DOWNLOAD_NEW_VERSION_PATH = "https://github.com/ShahNami/"+APPNAME+"/releases/download/";
	private SinemaConstant(){
		
	}
	
}
