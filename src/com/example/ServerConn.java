package com.example;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class ServerConn {
    private static String addr;
    private static int port;
    public void init()
    {
        /* init */
    }
    //设置服务器地址和端口值，连接服务器声明在线，被 ZeroManager 的 init()调用。

    //先设一个返回值，不然会报错
    public Vector<String> getOnlineNodeList()
    {
        Vector<String> NodeList = new Vector<String>();
        NodeList.add("User1");
        NodeList.add("User2");
        return NodeList;
    }
    //获取全部在线用户，在初始化和用户发送文件时调用。

    public void heartBeat()
    {  }
    /* 给服务器发送心跳包。*/
    /* 和 app 更新、校验码有关的方法添加在这里 */
}
