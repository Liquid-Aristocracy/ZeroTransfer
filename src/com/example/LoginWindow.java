package com.example;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JDialog {
    JLabel label = new JLabel("Node Name:");
    JTextField textField = new JTextField(20);
    JButton button = new JButton("OK");
    JTextArea info = new JTextArea(7,30);
    View view;

    //构造函数
    public LoginWindow(View v)
    {
        super(v, "Start ZeroTransfer", true);
        //setTitle("ZeroTransfer");
        view = v;
        setBounds(500,300,400,200);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout());
        setContentPane(contentPane);

        //添加控件
        contentPane.add(label);
        contentPane.add(textField);
        contentPane.add(button);
        contentPane.add(info);

        button.addActionListener((e) -> {
            onButtonOk();
        });
    }

    //事件处理
    private void onButtonOk()
    {
        String str = textField.getText();//获取输入内容
        //判断是否输入了
        if(!str.equals(""))
        {
            view.setZeroNodeName(str);
            button.setEnabled(false);
            new Thread(() -> {
                info.append("Starting ZeroTier Node ... \n\n");
                String initret = view.zmInit();
                info.append(initret);
                String servret = view.zmStartListener();
                info.append(servret);
                button.addActionListener((e) -> {
                    onFinish();
                });
                button.setEnabled(true);
            }).start();
        }
    }

    private void onFinish()
    {
        view.finishInit();
        this.dispose();
    }

}
