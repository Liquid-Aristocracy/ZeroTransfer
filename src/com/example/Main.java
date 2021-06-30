package com.example;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.io.IOException;

public class Main {

    private static final ZeroManager zm = new ZeroManager();

    public static void main(String[] args) throws IOException {
        //初始化结点，并加入 zerotier 虚拟网
        //zm.init();
        //开始监听是否有用户发来文件
        //zm.startListener();
        //加载界面的在线用户列表及传输文件列表(含发送文件功能)
        View frame = new View();
        frame.setVisible(true);
    }

}
