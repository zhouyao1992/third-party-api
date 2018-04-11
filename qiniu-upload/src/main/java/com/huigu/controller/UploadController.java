package com.huigu.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.qiniu.util.QiNiuApi;

/**
 * 7牛云文件上传
 * @author zhouyao
 */
@Controller
public class UploadController {
	@RequestMapping(value = "/uploadFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public Map<String, String> uploadFile(MultipartFile multfile, String uploadType) {
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		try {
			// 最终文件名
			String originalFilename = multfile.getOriginalFilename();
			String fileNameSuffix = originalFilename.substring(originalFilename.lastIndexOf(".")+1).toLowerCase();
			// 非法文件后缀校验
		    String enableSuffix = "jpg,jpeg,png,gif,avi,mp4,rm,rmvb,3gp,mpeg,mkv,wmv";
			if(!enableSuffix.contains(fileNameSuffix)){
				resultMap.put("respCode","0");
				resultMap.put("respDesc","非法文件格式");
				return resultMap;
			}
			
			String fileName =(UUID.randomUUID()+"." + fileNameSuffix).replace("-","");
			byte[] uploadBytes = multfile.getBytes();
			if ("0".equals(uploadType)) { //小文件例如：图片上传
				resultMap = QiNiuApi.uploadMinFile(uploadBytes, "image" + fileName);
			} else if ("1".equals(uploadType)) {//大文件：视频上传  
				File file = QiNiuApi.mult2file(multfile);
				resultMap = QiNiuApi.uploadMaxFile(file, "video" + fileName);
			}
		} catch (Exception e) {
			resultMap.put("respCode", "0");
			resultMap.put("respDesc", "系统异常");
		}

		return resultMap;
	}

	
	/**
	 * 7牛云文件下载
	 * @param filePath
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/downloadFile")
	public void downloadFile(String filePath, HttpServletResponse response) {
		InputStream in = null;
		OutputStream out = null;
		try {
			// path是指欲下载的文件的路径。
			File file = new File(filePath);
			// 取得文件名。
			String filename = file.getName().substring(0, file.getName().indexOf("?"));
		
			// 七牛云转化下载链接地址 windine.blogdriver.com/logo.gif
			//String downloadUrl = QiNiuApi.downPublicFile(filename);
			URL url = new URL(filePath);
			URLConnection conn = url.openConnection();
			// 以流的形式下载文件。
			in = new BufferedInputStream(conn.getInputStream());
			out = response.getOutputStream();
			int length = conn.getContentLength();
			byte[] buffer = new byte[length];
			int len = 0;
			// 设置浏览器文件流头部信息
			// 把文件名按UTF-8取出并按ISO8859-1编码，保证弹出窗口中的文件名中文不乱码，中文不要太多，最多支持17个中文，因为header有150个字节限制。
			// fileName = new String(fileName.getBytes("UTF-8"),"ISO8859-1");
			response.setContentType("application/x-msdownload");// 告诉浏览器输出内容为流
			response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
			response.addHeader("Content-Length", "" + length);

			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			out.flush();
			out.close();
			in.close();
		}catch(IOException ioe){
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取7牛云上传uploadToken
	 * @return
	 */
	@RequestMapping(value = "/getUploadToken")
	@ResponseBody
	public Map<String, String> getUploadToken(){
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		try {
			resultMap.put("respCode","1");
			resultMap.put("respDesc","获取文件上传token成功");
			String uploadToken = QiNiuApi.getUpToken();
			resultMap.put("uploadToken",uploadToken);
		}catch (Exception e) {
			resultMap.put("respCode","0");
			resultMap.put("respDesc","获取token失败");
		}
		return resultMap;
	}
	
	/**
	 * 将上传到7牛云的文件转化为防盗链下载链接地址
	 * @param fileName 7牛云所在文件的名称
	 * @return
	 */
	@RequestMapping(value = "/getAuthDownUrl")
	@ResponseBody
	public Map<String, String> getAuthDownUrl(String fileName){
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		try {
			resultMap.put("respCode","1");
			resultMap.put("respDesc","获取防盗链下载链接地址成功");
			String authDownUrl = QiNiuApi.getDownPublicFileUrl(fileName);
			resultMap.put("authDownUrl",authDownUrl);
		}catch (Exception e) {
			resultMap.put("respCode","0");
			resultMap.put("respDesc","获取防盗链下载链接地址失败");
		}
		return resultMap;
	}
	
}
