<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<form method="post" action="<%=basePath%>uploadFile" enctype="multipart/form-data">

 <%--  <input name="key" type="hidden" value="<resource_key>">
  <input name="x:<custom_name>" type="hidden" value="<custom_value>">
  <input name="token" type="hidden" value="<upload_token>">
  <input name="crc32" type="hidden" />
  <input name="accept" type="hidden" /> --%>
   图片文件：<input name="multfile" type="file" />
   类型：<input name="uploadType" type="text" value="0"/>
   <input  type="submit"  value="上传至7牛云"/>
  
</form>

<br><br><br><br><br>


<form method="post" action="<%=basePath%>uploadFile" enctype="multipart/form-data">

 <%--  <input name="key" type="hidden" value="<resource_key>">
  <input name="x:<custom_name>" type="hidden" value="<custom_value>">
  <input name="token" type="hidden" value="<upload_token>">
  <input name="crc32" type="hidden" />
  <input name="accept" type="hidden" /> --%>
   视频文件：<input name="multfile" type="file" />
   类型：<input name="uploadType" type="text" value="1"/>
   <input  type="submit"  value="上传至7牛云"/>
  
</form>

<br><br><br><br><br>



<form method="post" action="<%=basePath%>downloadFile">

  <img src="http://dcmcdn.szyyky.com/image439ae024dac64134924af72552fb4554.gif?sign=3672c3a207d01fd4148d4248d8dadce5&t=5a9e6e68" style="width: 80px;height: 80px"/>
  <input name="filePath" type="text" value="http://dcmcdn.szyyky.com/image439ae024dac64134924af72552fb4554.gif?sign=3672c3a207d01fd4148d4248d8dadce5&t=5a9e6e68"/>
  <input  type="submit"  value="下载7牛云文件"/>

</form>




<br><br><br><br><br>

<form method="post" action="<%=basePath%>downloadFile">


视频下载地址：<br><br>
<input name="filePath" style="width:500px;height: 100px"  type="text" value="http://dcmcdn.szyyky.com/video0b5648ee3e564e08b8650f962cd18318.wmv?sign=388a7d90dd563158dbdf35027c493b2e&t=5a9e6f72"/>

<!-- <input name="filePath" type="text"  style="width:300px;height: 100px" disabled="disabled"  value="http://dcmcdn.szyyky.com/video3cdd5e07e95f4eb5be4afca6c78101d1.wmv"/>  -->
 

 <input  type="submit"  value="下载7牛视频文件"/>

</form>


<br><br><br><br><br>
<form method="get" action="<%=basePath%>getUploadToken">

   <input  type="submit"  value="获取7牛云上传token"/>
  
</form>

<br><br><br><br><br>
<form method="get" action="<%=basePath%>getAuthDownUrl">

      文件名：<input name="fileName" type="text" />
   <input  type="submit"  value="生成防盗链下载链接地址"/>
  
</form>

</body>
</html>