package com.example.sonarqubescan.utils;

import org.checkerframework.checker.units.qual.A;
import org.codehaus.plexus.util.StringUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 11:45 上午
 */
public class StringUtil {

    /**
     * 去掉字符串指定的前缀
     * @param str 字符串名称
     * @param prefix 前缀数组
     * @return
     */

    public static String removePrefix(String str, String prefix) {
        if (StringUtils.isEmpty(str)) {
            return "";
        } else {
            if (null != prefix) {
                if (str.toLowerCase().matches("^" + prefix.toLowerCase() + ".*")) {
                    return str.substring(prefix.length());
                }
            }

            return str;
        }
    }
    /**
     *@description: 读取csv文件
     *@param:[fileName]
     *@return:void
     *@time:2022/1/16 2:42 下午
     */
    public static List<List<String>> readCsv(String fileName) {
        List<List<String>> lists = new ArrayList<>() ;
        try (BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String record;
            int line = 0;
            while ((record = file.readLine()) != null) {
                if(line == 0){
                    line++;
                    continue;
                }

                List<String> list = new ArrayList<>();
                String[] cells = record.split(",");
                list.add(cells[0]);
                list.add(cells[1]);
                lists.add(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lists;
    }


}
