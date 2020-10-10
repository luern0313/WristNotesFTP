package cn.luern0313.wristReaderFTP.model;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/7/6.
 */

@Getter
@Setter
public class TransferModel
{
    private String fileName;
    private String filePath;
    private File fileFile;
    private long fileSize;
    private String fileSuccess;

    public TransferModel(String fileName, String filePath, File fileFile)
    {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileFile = fileFile;
        this.fileSize = fileFile.length();
    }
}
