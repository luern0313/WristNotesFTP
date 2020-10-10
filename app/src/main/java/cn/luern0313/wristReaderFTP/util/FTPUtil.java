package cn.luern0313.wristReaderFTP.util;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 被 luern0313 创建于 2020/7/6.
 */

public class FTPUtil
{
    public static String uploadFiles(FTPClient ftpClient, File uploadFile)
    {
        if(!ftpClient.isConnected() || !ftpClient.isAvailable())
            return "FTP服务器被关闭";
        if(uploadFile == null || !uploadFile.exists())
            return "文件不存在";
        try
        {
            FileInputStream input = new FileInputStream(uploadFile);
            ftpClient.storeFile(uploadFile.getName(), input);
            input.close();
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
