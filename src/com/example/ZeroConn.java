package com.example;

import com.zerotier.sockets.ZeroTierInputStream;
import com.zerotier.sockets.ZeroTierOutputStream;
import com.zerotier.sockets.ZeroTierSocket;

import javax.swing.*;
import java.io.*;
import java.nio.file.Path;

public class ZeroConn {
    //建立好的连接，准备好文件传输了。
    public ZeroTierSocket conn;
    //这个值决定究竟是发送文件还是接收文件。
    private boolean sendrecv;
    //要发送文件的路径
    private File file;


    private int state =0;
    // 这个连接要传输的文件名（顺便加上传输量什么的），这个view要显示

    public void assign(ZeroTierSocket socket, boolean flag, File file)
    {
        this.conn = socket;
        this.sendrecv = flag;
        this.file = file;
    }

    public void run()
    {
        //super.run();
        if(!sendrecv) //接收文件，本机用户作为服务端监听
        {
            OutputStream os = null;
            PrintWriter pw = null;
            try {
                ZeroTierInputStream is = conn.getInputStream();

                // 将DataInputStream与套接字的输入流进行连接
                DataInputStream dataInputStream = new DataInputStream(is);
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
                byte[] buf = new byte[1024];
                int len;
                state = 1;
                while ((len = dataInputStream.read(buf)) != -1) {
                    dataOutputStream.write(buf, 0, len);
                }
                //强行写入输出流，因为有些带缓冲区的输出流要缓冲区满的时候才输出
                dataOutputStream.flush();// 刷新缓冲流

                conn.shutdownInput();// 关闭输入流

                os = conn.getOutputStream();
                pw = new PrintWriter(os);
                JOptionPane.showMessageDialog(null,"The file has been saved in"+file);
                //pw.println("文件已保存在"+file); 不太确定这样改行不行
                pw.flush();
                conn.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭相关资源
                try {
                    if (pw != null) {
                        pw.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                    state = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{     //发送文件，本机用户作为客户端
            try
            {
                //获取该Socket的输出流，用来向服务端发送文件
                ZeroTierOutputStream outputStream = conn.getOutputStream();

                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(String.valueOf(file)));


                //System.out.println("====== Start transferring file ====== ");
                JOptionPane.showMessageDialog(null,"====== Start transferring file ====== ");
                state = 1;
                byte[] buf = new byte[1024];
                int len;
                while ((len = dataInputStream.read(buf)) != -1) {
                    dataOutputStream.write(buf, 0, len);// 向dataOutputStream中写入数据
                }
                dataOutputStream.flush(); // 刷新缓冲流
                conn.shutdownOutput();  // 禁用此套接字的输出流

                // 获取输入流，取得服务器端的信息
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                //String info;
                /*while ((info = br.readLine()) != null) {
                    JOptionPane.showMessageDialog(null," ");
                    System.out.println("服务器端的信息：" + info);
                }*/
                //System.out.println("====== The file transfer was successful! ====== ");
                JOptionPane.showMessageDialog(null,"====== The file transfer was successful! ====== ");
                conn.shutdownInput();// 禁用此套接字的输出流

                // 关闭socket
                outputStream.close();
                dataInputStream.close();
                dataOutputStream.close();
                is.close();
                br.close();
                conn.close();
                state = 2;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public int getState() {
        return state;
    }

    public String getFilename() {
        assert file != null;
        // 0是还没准备好（等待对面同意发文件什么的），1是在发了，2是发完了（可以丢了）
        return file.getName();
    }

}
