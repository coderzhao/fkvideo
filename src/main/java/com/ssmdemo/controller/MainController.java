package com.ssmdemo.controller;

import com.ssmdemo.forward.Constant;
import com.ssmdemo.forward.FkvideoUtil;
import com.ssmdemo.forward.MethodUtil;
import com.ssmdemo.model.Customer;

import com.ssmdemo.service.inf.ICustomerService;
import com.ssmdemo.util.RuntimeLocal;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
//@RequestMapping("fkvideo")
public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class);

    String timestamp = null;
    int resultSize = 0;

    @Autowired
    ICustomerService customerService;

    @RequestMapping(value = "/ssmdemo/insert")
    @ResponseBody
    public int insert(String name,String password){
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPassword(password);
        return customerService.insert(customer);
    }
    @RequestMapping(value = "/ssmdemo/findAll")
    @ResponseBody
    public List<Customer> findAll(){

        return customerService.findAll();
    }

    /**
     *
     * @return
     */
    @RequestMapping(value = "/detector/**",method = RequestMethod.POST)
    @ResponseBody
    public String detector(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fkvideo_Detector");
        String API = request.getRequestURI().split("fkvideo/detector")[1];
        if(API.equals("/v0/face")||API.equals("/v0/identify")
                ||API.equals("/v1/face")) {
            request.setAttribute("API", API);
            String reply = FkvideoUtil.getInstance().requestFkvideo(request, response);
            return reply;
        }
        return "illegal_request";
    }

    /**
     *
     * @return
     */
    @RequestMapping(value = "/detector/**",method = RequestMethod.GET)
    @ResponseBody
    public void reset(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fkvideo_Detector");
        if(request.getRequestURI().contains("reset")) {
            request.getSession().removeAttribute("startTime");
            request.getSession().removeAttribute("result");
        }
    }

    @RequestMapping(value = "/anytec/**",method = RequestMethod.POST)
    @ResponseBody
    public String facenapi(HttpServletRequest request, HttpServletResponse response){
        String API = request.getRequestURI().split("fkvideo/anytec")[1];
        request.setAttribute("API", API);
        String reply= MethodUtil.getInstance().requestForward(request, response);
        return reply;
    }
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public String upload(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        FileItemFactory factory = new DiskFileItemFactory();
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        DataOutputStream dataOutputStream = null;
        String fileName = null;
        if (isMultiPart) {
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                Iterator<FileItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = iterator.next();
                    if (item.isFormField()) {
                        return "not a file";
                    } else {
                        String filedName = item.getFieldName();
                        fileName = item.getName();
//                        String filePath =request.getRealPath("video/")+fileName;
                        String filePath =Constant.VIDEO_PATH+fileName;
                        File file = new File(filePath);
                        dataOutputStream = new DataOutputStream(new FileOutputStream(file));
                        byte[] pic = item.get();
                        dataOutputStream.write(pic);
                    }
                }
            } catch (FileUploadException e) {
                response.setStatus(500);
                e.printStackTrace();
            } catch (IOException e) {
                response.setStatus(500);
                e.printStackTrace();
            } finally {
                if(dataOutputStream!=null) {
//                    String cmd="cp "+request.getRealPath("video/")+fileName +" /home/video/";
//                    new RuntimeLocal().execute(cmd);
//					dataOutputStream.close();
//					logger.info("开始复制文件到SDK服务器");
//					String cmd = new StringBuilder("scp ").append(request.getRealPath("video/")).append(fileName).append(" ")
//					.append(Constant.HOST).append(":").append(Constant.VIDEO_PATH).toString();
//					logger.info(cmd);
//					RuntimeLocal.exe(cmd);
                }
            }
        }
        return "success";
    }
    /**
     *
     * @return
     */
    @RequestMapping(value = "/upload",method = RequestMethod.GET)
    @ResponseBody
    public void servedAt(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Fkvideo_Detector");
        if(request.getRequestURI().contains("reset")) {
            request.getSession().removeAttribute("startTime");
            request.getSession().removeAttribute("result");
        }
    }
    /**
     *
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public String getResult(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        logger.info("开始获取session中数据");
        HttpSession session = request.getSession();
        if(session.getAttribute("result")==null||session.getAttribute("startTime")==null) {
            return "nil";
        }
        if(this.timestamp==null||!timestamp.equals(((Long) session.getAttribute("startTime")).toString()))
            timestamp = ((Long) session.getAttribute("startTime")).toString();
        ArrayList<String> resultList = (ArrayList<String>) session.getAttribute("result");
        int listSize = resultList.size();
        if(this.resultSize==0||listSize!=resultSize) {
            logger.info("数据更新");
            resultSize = listSize;
            StringBuilder stringBuilder = new StringBuilder("{");
            stringBuilder.append("\"begin\":\""+timestamp+"\",\"faces\":[");
            for(String s:resultList) {
                stringBuilder.append(s);
                if(!s.equals(resultList.get(resultList.size()-1)))
                    stringBuilder.append(",");
            }
            stringBuilder.append("]}");
            logger.info(stringBuilder.toString());
            return stringBuilder.toString();
        }
        logger.info("数据无变化");
        return "no change";
    }


    /**
     *
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.POST)
    @ResponseBody
    public String getResultPost(HttpServletRequest request, HttpServletResponse response) {
        logger.info("获取视频列表访问");
        response.setCharacterEncoding("utf-8");
//		ShellConnRemote shellConnRemote = new ShellConnRemote();
//		shellConnRemote.getSession();
        String path = Constant.VIDEO_PATH;
//		String ls = shellConnRemote.exeCommand("ls "+path);
        String ls = new RuntimeLocal().execute("ls "+path);
        String[] list = ls.split("\n");
        logger.info("视频列表长度"+list.length);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<list.length;i++) {
            stringBuilder.append(list[i]);
            if(i!=list.length-1)
                stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    /**
     *
     * @return
     */
    @RequestMapping(value = "/stopIdentify",method = RequestMethod.GET)
    @ResponseBody
    public String sessionControl(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if(session.getAttribute("stop")!=null) {
            session.removeAttribute("stop");
            return "";
        }
        session.setAttribute("stop", "stop");
        return "stop fkvideo_detector";
    }

}
