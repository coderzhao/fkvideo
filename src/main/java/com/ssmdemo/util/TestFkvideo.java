package com.ssmdemo.util;


public class TestFkvideo {

	public static void main(String[] args) throws InterruptedException {
	/*	String cmd = "fkvideo_detector --api-host "+Constant.API_HOST
				+" --api-token "+Constant.API_TOKEN
				+" --api-port "+Constant.API_PORT
				+" --license-ntls-server 192.168.10.208:3133 -S file@50:/home/testPic/111.flv --camid fkvideo"
				+" --body galleries=test"
				+" --request-url /fkvideo/detector/v0/identify";
		Runnable fkvideo = new FkvideoRunnable(cmd, new ShellConnRemote());
		Thread thread = new Thread(fkvideo);
		thread.setDaemon(true);
		thread.start();
		thread.join();
		System.out.println("end");*/
		ShellConnRemote shellConnRemote = new ShellConnRemote();
		shellConnRemote.getSession();
		System.out.println(shellConnRemote.exeCommand("fkvideo_detector --api-host 192.168.10.124 --api-token test --api-port 8080 --license-ntls-server 192.168.10.208:3133 -S file@:/home/video/111.flv --camid 1510292008311 --single-pass --body galleries=test --request-url /fkvideo/anytec/v0/identify"));
		shellConnRemote.close();
	}
	
}
