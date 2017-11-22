package com.ssmdemo.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RuntimeLocal {
	private static Logger logger  = Logger.getLogger(RuntimeLocal.class);
	private static final Runtime runtime = Runtime.getRuntime();
	public Process process = null;
	public static void main(String[] a) throws IOException {
		RuntimeLocal runtimeLocal = new RuntimeLocal();
		runtimeLocal.execute("fkvideo_detector --api-host 192.168.10.102 --api-token wB82-SEaX --api-port 8000 --license-ntls-server 192.168.10.102:3133 -S file@:/home/anytec-z/Videos/floral.mp4 --camid 1510630062609 --single-pass --scale 0.5 --tracker-threads 4 --body galleries=test --body threshold=0.64 --body mf_selector=all --request-url /v0/identify");
		try {
			Thread.sleep(7000);
			runtimeLocal.closeProcess();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String execute(String cmd) {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader br = null;
		  try {
			  String[] cmds = new String[] {
					  "/bin/sh","-c",cmd};
			  process = runtime.exec(cmds);
			  br = new BufferedReader(new InputStreamReader(
					  process.getInputStream(), "utf-8"));
			  String tmp = null;
			  while ((tmp = br.readLine()) != null) {
				  stringBuilder.append(tmp).append("\n");
			  }

		  } catch (IOException e) {
		  
		  }finally {
		  	if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		  }
		  return stringBuilder.toString();
	}
	public void closeProcess(){
		if (!process.isAlive())
			return;
		process.destroy();
	}
	public boolean isAlive(){
		return process.isAlive();
	}
}
