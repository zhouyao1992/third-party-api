package com.qiniu.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.gson.Gson;
import com.qiniu.cdn.CdnManager;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.persistent.FileRecorder;

/**
 * 7牛云测试账号参数信息 accessKey=tMeAo6A-Pcs3ZRoqDSNYBt38a_-La3RCNbRl1ieq
 * secretKey=MnaRl0tjjk97QPvEVePpzmm4BMlXTIJMZLPcQEKY bucket=dcmtest zone=0
 * url=http://dcmcdn.szyyky.com/
 * @author zhouyao
 */
public class QiNiuApi {
	// 用户凭证
	private static final String ACCESS_KEY = "tMeAo6A-Pcs3ZRoqDSNYBt38a_-La3RCNbRl1ieq";
	private static final String SECRET_KEY = "MnaRl0tjjk97QPvEVePpzmm4BMlXTIJMZLPcQEKY";
	// 要上传的空间
	private static final String BUCKET = "dcmtest";
	// 7牛云图片服务器地址
	private static final String QINIUYUN_URL = "http://dcmcdn.szyyky.com/";
	// 密钥配置
	private static final Auth AUTH = Auth.create(ACCESS_KEY, SECRET_KEY);
	// 设置断点续传的目录适用于大文件上传
	private static final String QINIU_UPFILE_TEMP = Thread.currentThread().getContextClassLoader().getResource("").getPath() + "upload/";

	// 构造私有空间的需要生成的下载的链接
	private static final String URL = "http://BUCKETdomain/key";

	/**
	 * 上传小文件例如：图片 以字节形式
	 * 
	 * @param file
	 */
	public static Map<String, String> uploadMinFile(byte[] uploadBytes, String fileName) {
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		String respCode = "0"; // 0 1 上传成功
		String respDesc = "上传失败";
		String filePath = "";// 文件所在7牛云的地址
		String authDownPath = "";//防盗链下载地址
		try {
			// 构造一个带指定Zone对象的配置类 Zone.autoZone()
			Configuration cfg = new Configuration(Zone.zone0());
			// ...其他参数参考类注释
			UploadManager uploadManager = new UploadManager(cfg);
			// 默认不指定key的情况下，以文件内容的hash值作为文件名
			String key = fileName;
			String upToken = getUpToken();
			Response response = uploadManager.put(uploadBytes, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			System.out.println(putRet.hash);// 图片Hash值
			System.out.println(putRet.key);// 图片名称
			String resulltKey = putRet.key;
			if (fileName.equals(resulltKey)) {
				System.out.println("文件上传成功");
				// 上传成功返回文件7牛云地址
				respCode = "1";
				filePath = QINIUYUN_URL + resulltKey;
				authDownPath = getDownPublicFileUrl(fileName);
				respDesc = "文件上传成功";
				System.out.println("====文件上传成功====地址" + filePath);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		resultMap.put("respCode", respCode);
		resultMap.put("filePath", filePath);
		resultMap.put("authDownPath", authDownPath);
		resultMap.put("respDesc", respDesc);
		return resultMap;
	}

	/**
	 * 上传大文件例如视频、音频 7牛云
	 */
	public static Map<String, String> uploadMaxFile(byte[] uploadBytes, String fileName) {
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		String respCode = "0"; // 0 1 上传成功
		String filePath = "";// 文件所在7牛云的地址
		String authDownPath = "";//防盗链下载地址
		String respDesc = "";
		try {
			// 构造一个带指定Zone对象的配置类 Zone.autoZone()
			Configuration cfg = new Configuration(Zone.zone0());
			// 默认不指定key的情况下，以文件内容的hash值作为文件名
			String key = fileName;
			String upToken = getUpToken();
			// 设置断点续传的目录
			String localTempDir = QINIU_UPFILE_TEMP;
			// 设置断点续传文件进度保存目录
			FileRecorder fileRecorder = new FileRecorder(localTempDir);
			UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
			Response response = uploadManager.put(uploadBytes, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			System.out.println(putRet.key);
			System.out.println(putRet.hash);
			String resulltKey = putRet.key;
			if (fileName.equals(resulltKey)) {
				System.out.println("文件上传成功");
				// 上传成功返回文件7牛云地址
				respCode = "1";
				filePath = QINIUYUN_URL + resulltKey;
				authDownPath = getDownPublicFileUrl(fileName);
				respDesc = "文件上传成功";
				System.out.println("====文件上传成功====地址" + filePath);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		resultMap.put("respCode", respCode);
		resultMap.put("filePath", filePath);
		resultMap.put("authDownPath", authDownPath);
		resultMap.put("respDesc", respDesc);
		return resultMap;
	}

	
	
	
	/**
	 * 分片上传
	 * @param file
	 * @param fileName
	 * @return
	 */
	public static Map<String, String> uploadMaxFile(File file, String fileName) {
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		String respCode = "0"; // 0 1 上传成功
		String respDesc = "上传失败";
		String filePath = "";// 文件所在7牛云的地址
		String authDownPath = "";//防盗链下载地址
		try {
			// 构造一个带指定Zone对象的配置类 Zone.autoZone()
			Configuration cfg = new Configuration(Zone.zone0());
			// ...其他参数参考类注释
			UploadManager uploadManager = new UploadManager(cfg);
			// 默认不指定key的情况下，以文件内容的hash值作为文件名
			String key = fileName;
			String upToken = getUpToken();
			Response response = uploadManager.put(file, key, upToken, null, null, false);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			System.out.println(putRet.hash);// 图片Hash值
			System.out.println(putRet.key);// 图片名称
			String resulltKey = putRet.key;
			if (fileName.equals(resulltKey)) {
				System.out.println("文件上传成功");
				// 上传成功返回文件7牛云地址
				respCode = "1";
				filePath = QINIUYUN_URL + resulltKey;
				authDownPath = getDownPublicFileUrl(fileName);
				respDesc = "文件上传成功";
				System.out.println("====文件上传成功====地址" + filePath);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		resultMap.put("respCode", respCode);
		resultMap.put("filePath", filePath);
		resultMap.put("authDownPath", authDownPath);
		resultMap.put("respDesc", respDesc);
		return resultMap;
	}
	
	/**
	 * 下载7牛公开文件
	 */
	public static String getDownPublicFileUrl(String fileName) throws Exception {
		String domainOfBUCKET = QINIUYUN_URL;
		String encodedFileName = URLEncoder.encode(fileName, "utf-8");
		String originalUrl = String.format("%s%s", domainOfBUCKET, encodedFileName);
		//链接过期时间
		long deadline = System.currentTimeMillis() / 1000 + 3600;
		URL url = new URL(originalUrl);
		//生成防盗链下载链接
		String downloadUrl = CdnManager.createTimestampAntiLeechUrl(url,SECRET_KEY, deadline);
		 
		System.out.println(downloadUrl);
		return downloadUrl;
	}

	/**
	 * 下载7牛私有文件
	 */
	public static String getDownPrivateFileUrl(String fileName) throws Exception {
		// String fileName = "七牛/云存储/qiniu.jpg";
		// String domainOfBUCKET = "http://devtools.qiniu.com";
		String domainOfBUCKET = QINIUYUN_URL;
		String encodedFileName = URLEncoder.encode(fileName, "utf-8");
		String publicUrl = String.format("%s/%s", domainOfBUCKET, encodedFileName);
		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
		long expireInSeconds = 3600;// 1小时 ，可以自定义链接过期时间
		String downloadUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
		System.out.println(downloadUrl);
		return downloadUrl;
	}

	/**
	 * 上传小文件例如：图片
	 */
	public static void uploadMinFile(String fileName) {

		// 构造一个带指定Zone对象的配置类 Zone.autoZone()
		Configuration cfg = new Configuration(Zone.zone0());
		// ...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		// 如果是Windows情况下，格式是 D:\\qiniu\\test.png
		String localFilePath = "/home/qiniu/test.png";
		// 默认不指定key的情况下，以文件内容的hash值作为文件名
		String key = fileName;
		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
		String upToken = auth.uploadToken(BUCKET);
		try {
			Response response = uploadManager.put(localFilePath, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			System.out.println(putRet.key);
			System.out.println(putRet.hash);
		} catch (QiniuException ex) {
			Response r = ex.response;
			System.err.println(r.toString());
			try {
				System.err.println(r.bodyString());
			} catch (QiniuException ex2) {
				// ignore
			}
		}

	}

	/**
	 * 将文件转化成字节数组
	 * 
	 * @param file
	 * @param fileName
	 * @return
	 */
	public  static byte[] File2byte(File file, String fileName) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	
	public static File mult2file(MultipartFile multfile) {
		
		    CommonsMultipartFile cf = (CommonsMultipartFile)multfile;   
		     //这个myfile是MultipartFile的  
		    DiskFileItem fi = (DiskFileItem) cf.getFileItem();  
		    File file = fi.getStoreLocation(); 
		    return file;
	}
	
	// 获取7牛云token
	public static String getUpToken() {

		return AUTH.uploadToken(BUCKET);

	}

}
