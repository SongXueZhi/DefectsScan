package com.example.sonarqubescan.utils;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/16 11:24 上午
 */

public class FileFilter {
    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info.java";

    private static boolean baseFilenameFilter(String path, String str) {
        return path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                path.toLowerCase().contains("lib/") ||
                str.toLowerCase().startsWith("test");
    }

    private static String getFilename(String path) {
        String[] strs = path.split("/");
        return strs[strs.length - 1].toLowerCase();
    }

    /**
     * true: 过滤
     * false： 不过滤
     */
    public static boolean javaFilenameFilter(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        String str = getFilename(path);
        return baseFilenameFilter(path, str) ||
                !str.toLowerCase().endsWith(".java") ||
                str.toLowerCase().endsWith("test.java") ||
                str.toLowerCase().endsWith("tests.java") ||
                str.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
    }


}
