package com.example;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileAccessor {

    // 是不是要改成path?
    //TODO 文件发送功能放zeroconn里了
    //private final Path fileSavingPath = Paths.get(System.getProperty("user.dir")); // working dir

    public File filePicker () {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose File ");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            //选择文件目录为 selectedFile
            if (selectedFile.isFile()) {
                return selectedFile;
            }
        }
        return null;
    }

    // TODO 没用这个，发送文件和接收文件我都用ZeroConn来搞了
    // TODO
    /*public boolean fileSaver (String filename, byte[] filecontent) {

        return false;
    }*/

}
