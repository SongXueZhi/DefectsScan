package com.example.sonarqubescan.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 2:13 下午
 */

public class FileUtil {

    public static Boolean deleteFile(File dirFile){
        if(!dirFile.exists()){
            return false;
        }
        if(dirFile.isFile()){
            return dirFile.delete();
        }else{
            for(File file : Objects.requireNonNull(dirFile.listFiles())){
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            assert inputChannel != null;
            inputChannel.close();
            assert outputChannel != null;
            outputChannel.close();
        }
    }

    public static File createFile(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException e) {

            e.printStackTrace();


        }
        return null;
    }
}
