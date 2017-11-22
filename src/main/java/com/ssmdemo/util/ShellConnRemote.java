package com.ssmdemo.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ssmdemo.forward.Constant;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

public class ShellConnRemote {
	
	private static final Logger logger = Logger.getLogger(ShellConnRemote.class);
	
	static final String host = Constant.HOST;
	static final String userName = null;
	static final String password = Constant.PASSWORD;
	
	static ChannelExec channelExec = null;
	static Session session = null;
	
	public void close() {
		 channelExec.disconnect();
	        if (null != session) {
	            session.disconnect();
	        }
	}
	public String exeCommand(String cmd) {
		channelExec.setCommand(cmd);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);
        StringBuilder sBuilder = new StringBuilder();
        try {
			channelExec.connect();
			InputStream in = channelExec.getInputStream();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		    String buf = null;
		    while ((buf = reader.readLine()) != null) {
		        sBuilder.append(buf+"\n");
		    }
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return sBuilder.toString();
	}
	public void getSession() {
		JSch jsch = new JSch(); 
        int port = 22;
		try {
			session = jsch.getSession(userName, host, port);
			  session.setPassword(password); 
		        Properties config = new Properties();
		        config.put("StrictHostKeyChecking", "no");
		        session.setConfig(config); 
		        int timeout = 60000000;
		        session.setTimeout(timeout);
		        session.connect();
		        if(session.isConnected())
		        	logger.info("fk主机已连接");
		        channelExec = (ChannelExec) session.openChannel("exec");
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
	public int getExitStatus() {
		return this.channelExec.getExitStatus();
	}
   
}