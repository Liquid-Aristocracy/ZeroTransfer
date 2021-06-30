package com.example;

import javax.swing.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;;

public class ServerConn {
    private static String addr;
    private static int port;
    private final String USER_AGENT = "Mozilla/5.0";
    public void init()
    {
<<<<<<< HEAD
        ServerConn.addr = "server.zerotier"; // Change this later
        ServerConn.port = 8888;
=======
        this.addr = "server.zerotier"; // Change this later
        this.port = 8888;
>>>>>>> f5e94bc1a8da165c1a8bda3d8712fc8ca6b62a93
        /* init */
    }
    //设置服务器地址和端口值，连接服务器声明在线，被 ZeroManager 的 init()调用。

    //先设一个返回值，不然会报错
    public Vector<String> getOnlineNodeList() throws ClientProtocolException, IOException
    {
        String url = ServerConn.addr + "/userList";

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        //添加请求头
        request.addHeader("User-Agent", USER_AGENT);

        // Get json from response
        HttpResponse response = client.execute(request);
        // Need FastJson here
        String data = EntityUtils.toString(response.getEntity());
        JSONObject json = JSON.parseObject(data);
        Vector<String> list = new Vector<String>(JSON.parseArray(json.getJSONArray("users").toJSONString(), String.class));
        
        return list;
    }
    
    //获取全部在线用户，在初始化和用户发送文件时调用。

    public void heartBeat()
    {
    	String url = ServerConn.addr + "/online";
    	HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
    	URI uri = new URIBuilder(request.getURI()).addParameter("ip", ZeroManager.nodeIP).addParameter("id", ZeroManager.nodeId).build();
        ((HttpRequestBase) request).setURI(uri);

        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);
        
        return;
    }
    /* 给服务器发送心跳包。*/
    /* 和 app 更新、校验码有关的方法添加在这里 */
}


