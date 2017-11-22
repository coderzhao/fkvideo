package com.ssmdemo.forward;


import com.ssmdemo.util.ConfigManager;

public class Constant {
	
	private static ConfigManager sdk = ConfigManager.getInstance();
	public static final String TOKEN = sdk.getParameter("TOKEN");
	public static final String SDK_IP = sdk.getParameter("SDK_IP");
	

	//截取人脸的post地址及API
	public static final String API_HOST = sdk.getParameter("API_HOST");
	public static final String API_TOKEN = sdk.getParameter("API_TOKEN");
	public static final String API_PORT = sdk.getParameter("API_PORT");
	public static final String LICENSE_NTLS_SERVER = sdk.getParameter("LICENSE_NTLS_SERVER");
	public static final String REQUEST_URI = sdk.getParameter("REQUEST_URI");
	//fkvideo_detector_param
	public static final String TRACKER_THREADS = sdk.getParameter("TRACKER_THREADS");
	public static final String SCALE = sdk.getParameter("SCALE");
	
	public static final String VIDEO_PATH = sdk.getParameter("VIDEO_PATH");
	public static final String TIME_PLUS_HOURS = sdk.getParameter("TIME_PLUS_HOURS");
	
	public static final String DELETE_GALLERY_SH = sdk.getParameter("DELETE_GALLERY_SH");

}
