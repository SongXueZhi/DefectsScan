package com.example.sonarqubescan.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 5:39 下午
 */

public class AstUtil {
    public static String getCode(int startLine, int endLine, String filePath) {
        StringBuilder code = new StringBuilder();
        String s = "";
        int line = 1;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while ((s = bufferedReader.readLine()) != null) {
                if (line >= startLine && line <= endLine) {
                    code.append(s);
                    code.append("\n");
                }
                line++;
                if (line > endLine) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code.toString();
    }

    public static int getCodeLines(String filePath) {
        int result = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while (bufferedReader.readLine() != null) {
                result++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
