package com.example.sonarqubescan.utils;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Component;

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

}
