package com.example;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import com.zerotier.sockets.*;

import javax.swing.*;

import java.lang.Thread;
import java.io.IOException;

public class ZeroManager {
    String nodeId;
    String nodeIP;
    String nodeName;
    String fileName;

    //zerotier虚拟局域网的标识a09acf02333322cc
    long networkId;
    public ZeroManager()
    {
        networkId = 0xa09acf02333322ccL;
    }

    //这个类初始化的 tcp 服务器
    ZeroTierServerSocket listener;

    //监听的连接socket
    public ZeroTierSocket conn;

    //正在进行的全部连接的状态 -> 设为全局变量
    //public ZeroConn[] connlist;
    Vector<ZeroConn> connlist = new Vector<>();

    public String init() {

        ZeroTierNode node = new ZeroTierNode();
        //存储身份标识
        //node.initFromStorage(storagePath);

        node.start();

        //判断node是否成功联机
        while (!node.isOnline()) {
            ZeroTierNative.zts_util_delay(1000);
        }

        nodeId = Long.toHexString(node.getId());

        //加入network
        node.join(networkId);

        //确保node已被分配IP和路由
        while (!node.isNetworkTransportReady(networkId)) {
            ZeroTierNative.zts_util_delay(1000);
        }

        nodeIP = node.getIPv4Address(networkId).getHostAddress();

        return "You have joined the VLAN! \nYour Node ID:" + nodeId + "\nYour Node IP:"+ nodeIP + "\n\n";
    }

    public void setNodeName (String name) {
        nodeName = name;
    }

    //用户想发送一个文件的时候调用，会在 connlist 中增加一个连接，view 层可以显示这个连接的状态、文件传输速率等等。
    public void newConn(String remoteAddr, int port, File file) throws IOException {
        //创建客户端的Socket，指定服务器的IP和端口
        ZeroTierSocket socket = new ZeroTierSocket(remoteAddr, port);

        ZeroConn zc_send = new ZeroConn();
        zc_send.assign(socket, true, file, nodeName);
        //加入conn列表(用集合，便于删除)
        connlist.add(zc_send);
        //connlist.add(zc_send);

        //调用ZeroConn传输文件
        zc_send.run();
        JOptionPane.showMessageDialog(null,"File sending...");
        //System.out.println(" File sending... ");

    }

    // 就不单独把一整个类extend thread了，这样开thread也行
    // 这个方法run一个监听服务器的thread，跟原来的run一样，run不要了写到这里
    public String startListener()  {
        //调用ServerConn类的init函数初始化服务器
        new Thread(() -> {
            ServerConn sc = new ServerConn();
            sc.init();

            try {
                listener = new ZeroTierServerSocket(9999);
            } catch (IOException e) {
                e.printStackTrace();
            }

            JOptionPane.showMessageDialog(null,"The server has started, waiting for the client to connect...");
            //System.out.println("服务器启动，等待客户端的连接。。。");

            //循环监听等待客户端的连接
            while (true) {
                try {
                    conn = listener.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //如果有新连接，则抛出一个线程发送文件，自己继续监听
                if (conn != null)
                    new Thread(() -> {
                        // 弹出对话框：用户XXX向您发来文件，是否接收？
                        // TODO 这里用的是用户的IP，要改成ID不？怎么获得？
                        int userOption = JOptionPane.showConfirmDialog(null, "User (IP:"+conn.getRemoteAddress()+") has sent a document.Do you want to receive it?", "File Receipt Reminder",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        // 若选择接收, TODO 选择保存文件目录 or 默认保存目录
                        if (userOption == JOptionPane.OK_OPTION) {
                            File f = new File(System.getProperty("user.dir"));

                            ZeroConn zc_resc = new ZeroConn();
                            zc_resc.assign(conn, false, f, nodeName);
                            zc_resc.run();
                            connlist.add(zc_resc);
                        }

                    }).start();

            }
        });
        manageConnList();
        return "TCP Server started.";
    }

        // TODO 这个方法run一个管理connlist的thread，怎么样这样就有两个thread可以一起run了
        // TODO 没明白
    public void manageConnList(){
        new Thread(() -> {
            while (true) {
                connlist.forEach((conn) -> {
                    int state = conn.getState();
                    if (state == 2)
                        connlist.remove(conn);
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Vector<String> getConnListAsVector() {
        // 这个应该是让View获取conn列表然后显示的
        Vector<String> connData = new Vector<>();
        // 以在线用户的 remoteAddr 作为传输文件列表展示的标识
        for (ZeroConn zeroConn : connlist) {
            connData.add(
                    "RemoteAddr:"+zeroConn.conn.getRemoteAddress().toString()
                            +"fileName:"+zeroConn.getFilename()
            );
        }
        // TODO 传输文件速率，这个待会搞
        //long start=System.currentTimeMillis();
        //long end=System.currentTimeMillis();
        //System.out.println("传输时间"+(end-start)+"毫秒");
        // TODO 这个是干啥用的？
        connData.add("filename 2mb/4mb");

        return connData;
    }
}
