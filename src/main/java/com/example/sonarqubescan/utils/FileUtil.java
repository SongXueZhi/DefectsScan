package com.example.sonarqubescan.utils;

import java.io.File;
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
}
