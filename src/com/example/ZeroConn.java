package com.example;

import com.zerotier.sockets.ZeroTierInputStream;
import com.zerotier.sockets.ZeroTierOutputStream;
import com.zerotier.sockets.ZeroTierSocket;

import javax.swing.*;
import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;

import java.util.Arrays;

public class ZeroConn {
    //建立好的连接，准备好文件传输了。
    public ZeroTierSocket conn;
    //这个值决定究竟是发送文件还是接收文件。
    private boolean sendrecv;
    //要发送文件的路径
    private File file;
    private String nodeName;

    private int state =0;

    private String fileInfo;
    // 这个连接要传输的文件名（顺便加上传输量什么的），这个view要显示

    private long percentage;

    public void assign(ZeroTierSocket socket, boolean flag, File file, String NodeName)
    {
        this.conn = socket;
        this.sendrecv = flag;
        this.file = file;
        this.nodeName = NodeName;
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

                //先接收用户信息：nodeName、fileName、fileSize
                byte[] buf1 = new byte[1024];
                byte[] buf2 = new byte[1024];
                byte[] buf3 = new byte[1024];

                int len1, len2, len3;

                while ((len1 = dataInputStream.read(buf1)) != -1) {
                    dataOutputStream.write(buf1, 0, len1);
                }
                dataOutputStream.flush();// 刷新缓冲流
                nodeName = Arrays.toString(buf1);

                while ((len2 = dataInputStream.read(buf2)) != -1) {
                    dataOutputStream.write(buf2, 0, len2);
                }
                dataOutputStream.flush();// 刷新缓冲流
                String fileName = Arrays.toString(buf2);

                while ((len3 = dataInputStream.read(buf3)) != -1) {
                    dataOutputStream.write(buf3, 0, len3);
                }
                dataOutputStream.flush();// 刷新缓冲流
                String fileSize = Arrays.toString(buf3);

                //询问用户：有用户 nodeName 向您发来文件 fileName,文件大小为 fileSize，是否接收？
                int userOption = JOptionPane.showConfirmDialog(
                        null, "User (nodeName:" + nodeName + ") has sent a document(name:" + fileName + ", Size:" + fileSize + ")." +
                                "\nDo you want to receive it?", "File Receipt Reminder",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                // 若选择接收, 默认保存目录，开始接收文件
                if (userOption == JOptionPane.OK_OPTION) {
                    // TODO 返回客户端一个 OK 信息：这个不太确定
                    PrintWriter pws=new PrintWriter(conn.getOutputStream(),true);
                    pws.println("OK");

                    //默认保存目录
                    File f = new File(System.getProperty("user.dir"));

                    //已传输文件大小
                    long ycSize = 0;
                    //文件总大小
                    long sumSize = dataInputStream.readLong();

                    byte[] buf = new byte[1024];
                    int len;
                    state = 1;
                    while ((len = dataInputStream.read(buf)) != -1) {
                        dataOutputStream.write(buf, 0, len);
                        ycSize += dataInputStream.read(buf);
                        percentage = ycSize * 100L / sumSize;
                    }
                    //强行写入输出流，因为有些带缓冲区的输出流要缓冲区满的时候才输出
                    dataOutputStream.flush();// 刷新缓冲流

                    conn.shutdownInput();// 关闭输入流

                    os = conn.getOutputStream();
                    pw = new PrintWriter(os);
                    JOptionPane.showMessageDialog(null, "The file has been saved in" + file);
                    //pw.println("文件已保存在"+file); 不太确定这样改行不行
                    pw.flush();
                    conn.shutdownOutput();
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


            } catch (SocketException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{     //发送文件，本机用户作为客户端
            try {

                //获取该Socket的输出流，用来向服务端发送文件
                ZeroTierOutputStream outputStream = conn.getOutputStream();

                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(String.valueOf(file)));


                byte[] buf1 = nodeName.getBytes();
                byte[] buf2 = file.getName().getBytes();
                byte[] buf3 = toByteArray(file.length());

                int len1, len2, len3;
                while ((len1 = dataInputStream.read(buf1)) != -1) {
                    dataOutputStream.write(buf1, 0, len1);// 向dataOutputStream中写入数据
                }
                dataOutputStream.flush(); // 刷新缓冲流

                while ((len2 = dataInputStream.read(buf2)) != -1) {
                    dataOutputStream.write(buf2, 0, len2);// 向dataOutputStream中写入数据
                }
                dataOutputStream.flush(); // 刷新缓冲流

                while ((len3 = dataInputStream.read(buf3)) != -1) {
                    dataOutputStream.write(buf3, 0, len3);// 向dataOutputStream中写入数据
                }
                dataOutputStream.flush(); // 刷新缓冲流

                // 等待服务器的 OK ，再发送文件
                InputStreamReader isr = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String response = br.readLine();

                if (response.equals("OK")) {   //客户端收到OK，开始发送文件
                    //已传输文件大小
                    int ycSize = 0;
                    //文件总大小
                    long sumSize = dataInputStream.readLong();

                    //System.out.println("====== Start transferring file ====== ");
                    JOptionPane.showMessageDialog(null, "====== Start transferring file ====== ");
                    state = 1;
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = dataInputStream.read(buf)) != -1) {
                        dataOutputStream.write(buf, 0, len);// 向dataOutputStream中写入数据
                        ycSize += dataInputStream.read(buf);
                        percentage = ycSize * 100L / sumSize;
                    }

                    dataOutputStream.flush(); // 刷新缓冲流

                    conn.shutdownOutput();  // 禁用此套接字的输出流

                    // 获取输入流，取得服务器端的信息
                    InputStream is = conn.getInputStream();
                    BufferedReader brc = new BufferedReader(new InputStreamReader(is));

                    //System.out.println("====== The file transfer was successful! ====== ");
                    JOptionPane.showMessageDialog(null, "====== The file transfer was successful! ====== ");
                    conn.shutdownInput();// 禁用此套接字的输出流

                    // 关闭socket
                    outputStream.close();
                    dataInputStream.close();
                    dataOutputStream.close();
                    is.close();
                    brc.close();
                    conn.close();
                    state = 2;
                }
                else { //返回"发送失败"
                    JOptionPane.showMessageDialog(null, "Failed to send.");
                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
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
        fileInfo = file.getName() + percentage;
        return fileInfo;
    }

    public static byte[] toByteArray(long value) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
    }
}
