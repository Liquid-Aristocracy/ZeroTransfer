package com.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class View extends JFrame {
    private final JList<String> onlinePeerList;
    private final JList<String> currentConnList;
    private FileAccessor fileAccessor;
    private ZeroManager zeroManager;
    private ServerConn  serverConn;
    private boolean finishedInit;

    public View() throws IOException {
        setTitle("ZeroTransfer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(400,200,1000,600);
        JPanel contentPane=new JPanel();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        contentPane.setLayout(new BorderLayout(0,0));
        setContentPane(contentPane);

        //在线用户列表
        onlinePeerList = new JList<>();
        onlinePeerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane onlinePeerPane = new JScrollPane();
        contentPane.add(onlinePeerPane, BorderLayout.WEST);
        onlinePeerPane.setViewportView(onlinePeerList);
        //监听列表选择事件
        onlinePeerList.addListSelectionListener(e -> {
            try {
                handleFilePick(e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        //正在传输文件列表
        currentConnList = new JList<>();
        currentConnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane connPane = new JScrollPane();
        contentPane.add(connPane, BorderLayout.CENTER);
        connPane.setViewportView(currentConnList);

        zeroManager = new ZeroManager();
        finishedInit = false;
        fileAccessor = new FileAccessor();
        serverConn = new ServerConn();

        LoginWindow login = new LoginWindow(this);
        login.setVisible(true);
        if (!finishedInit) {
            System.exit(0);
        }

        //更新
        updateWindow();
    }

    public void init () {


    }

    private void updateWindow(){
        new Thread(() -> {
            Vector<String> peerData;
            //peerData.add("123.45.67.8");
            //peerData.add("123.45.67.9");
            try {
                peerData = serverConn.getOnlineNodeList();
            } catch (IOException e) {
                e.printStackTrace();
                peerData = null;
            }
            while (true) {
                onlinePeerList.setListData(peerData);
                // 连接服务器获取在线用户列表并且刷新

                Vector<String> connData;
                connData = zeroManager.getConnListAsVector();
                currentConnList.setListData(connData);
                // 从 zeromanager 获取连接列表并刷新

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //点击之后发送文件
    protected void handleFilePick(ListSelectionEvent event) throws IOException {
        if (!event.getValueIsAdjusting() && onlinePeerList.getSelectedIndex() >= 0) { // 松开才响应
            String remoteIP = onlinePeerList.getSelectedValue();
            File fileSend = fileAccessor.filePicker();

            if (fileSend != null) {
                // TODO 如何获得用户 port
                int port = 0;
                //发送文件
                zeroManager.newConn(remoteIP, port, fileSend);
            }
        }
    }

    public void setZeroNodeName (String name) {
        zeroManager.setNodeName(name);
    }

    public String zmInit () {
        return zeroManager.init();
    }

    public String zmStartListener () {
        return zeroManager.startListener();
    }

    public void finishInit () {
        finishedInit = true;
    }

    public boolean getFinishInit () {
        return finishedInit;
    }

    // TODO 这函数哪里用到了
    public JList<String> getConnlist()
    {
        return currentConnList;
    }
}
