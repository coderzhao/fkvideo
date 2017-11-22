package com.ssmdemo.forward;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ssmdemo.util.RuntimeLocal;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;


import com.ssmdemo.util.FkvideoRunnable;

public class FkvideoUtil {
	private static ServletContext application;
	private static FkvideoUtil instance;
	private final Logger logger = Logger.getLogger(FkvideoUtil.class);

	private static FileItemFactory factory = new DiskFileItemFactory();
	private static Map<String, String> header = new HashMap<String, String>();
	private static Map<String, Object> param = new HashMap<String, Object>();
	private static Map<String, Object> file = new HashMap<String, Object>(1);

	private FkvideoUtil() {}

	public static FkvideoUtil getInstance() {    //对获取实例的方法进行同步
		if (instance == null) {
			synchronized (FkvideoUtil.class) {
				if (instance == null)
					instance = new FkvideoUtil();
			}
		}
		return instance;
	}

	public String requestFkvideo(HttpServletRequest request,
								 HttpServletResponse response) {
		logger.info("*************START***********");
		String API = (String) request.getAttribute("API");
		header.clear();
		param.clear();
		file.clear();
		String reply = "0";
		application = request.getSession().getServletContext();
		//判断是否有文件输入
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
		if (isMultiPart) {
			logger.info("********* HAS FILE********");
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				List<FileItem> items = upload.parseRequest(request);
				Iterator<FileItem> iterator = items.iterator();
				while (iterator.hasNext()) {
					FileItem item = iterator.next();
					if (item.isFormField()) {
						//文本
						String filedName = item.getFieldName();
						String value = item.getString("utf-8");
						param.put(filedName, value);
						logger.info("文本域：" + filedName + "——" + value);
						//有上传文件
					} else {
						file.put("contentType", item.getContentType());
						logger.info("contentType:" + item.getContentType());
						String filedName = item.getFieldName();
						String fileName = item.getName();
						logger.info("文件控件: " + filedName + "--" + fileName);
						byte[] pic = item.get();
						logger.info("PICTURE.LENGTH:" + pic.length);
						file.put(filedName, pic);
					}
				}
			} catch (FileUploadException e) {
				response.setStatus(500);
				logger.error("*****FILE_UPLOAD_FAIL*****");
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		//向session中添加待比对图片
		if(file.containsKey("img")){
			logger.info("添加待比对人脸图片");
			application.setAttribute("img",file.get("img"));
		}
		//获取表单文本数据
		Enumeration<String> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			logger.info("*********HAS INPUT*********");
			String name = enumeration.nextElement();
			param.put(name, request.getParameter(name));
			logger.info("PARAM:" + name + "--" + "" + param.get(name));
		}
		//配置命令参数并启动fkvideo组件
		String camid = ((Long)System.currentTimeMillis()).toString();
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(300);
		StringBuilder cmdBuilder = new StringBuilder("fkvideo_detector --api-host ");
		cmdBuilder.append(Constant.API_HOST)
		.append(" --api-token ").append(Constant.API_TOKEN)
		.append(" --api-port ").append(Constant.API_PORT)
		.append(" --license-ntls-server ").append(Constant.LICENSE_NTLS_SERVER)
		.append(" -S file@:").append(Constant.VIDEO_PATH).append((String)param.get("videoName"))
		.append(" --camid ").append(camid)
		//fkvideo参数设置
		.append(" --single-pass")

		//====================
		.append(" --post-uniq 1")
		.append(" --uc-max-time-diff 1")
		.append(" --uc-max-dup 4")
		.append(" --uc-max-avg-shift 10")
		//====================
		.append(" --scale ").append(Constant.SCALE)
		.append(" --min-face-size 50")
		.append(" --disable-drops 1")
		.append(" --min-score -7")
		.append(" --max-persons 4")
		.append(" --draw-track 0")
		.append(" --tracker-threads ").append(Constant.TRACKER_THREADS)
		.append(" --body threshold=").append((String)param.get("threshold"))
		.append(" --body mf_selector=all")
		.append(" --sink-url rtmp://localhost:1935/livecam")
		.append(" --request-url ").append(Constant.REQUEST_URI);
		String cmd = cmdBuilder.toString();
		cmd = cmd.replaceAll("\n","");
		cmd = cmd.replaceAll("\r","");
		logger.info(cmd);
		RuntimeLocal runtimeLocal = new RuntimeLocal();
		Runnable fkvideo = new FkvideoRunnable(cmd,runtimeLocal);
		Thread thread = new Thread(fkvideo);
		thread.setDaemon(true);
		thread.start();
		String finish = "finished";
		try {
		Thread.sleep(3000);
		session.setAttribute("startTime",System.currentTimeMillis());
		logger.info("正在进行视频检测");

//			Thread.sleep(5000);
			while(true) {
				Thread.sleep(2000);
				if(application.getAttribute(camid)!=null) {
					ArrayList<String> replyList = (ArrayList<String>) application.getAttribute(camid);
					session.setAttribute("result",replyList);
				}
				if(!runtimeLocal.isAlive()){
					runtimeLocal.closeProcess();
					break;
				}
				if(session.getAttribute("stop")!=null) {
					runtimeLocal.closeProcess();
					logger.info("停止fkvideo组件");
					session.removeAttribute("stop");
					finish = "stop";
					break;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("开始清除Session中数据");
			try {
				request.getSession().removeAttribute("result");
				if(request.getSession().getAttribute("result")==null)
					response.getWriter().println("resetSession successful");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return finish;
	}
	
}
