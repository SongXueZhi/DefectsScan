package com.example.sonarqubescan.utils;

import com.example.sonarqubescan.domin.enums.CompileToolEnum;
import com.example.sonarqubescan.domin.enums.JdkVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 3:55 下午
 */
@Slf4j
@Component
public class CompileUtil {
    private static String gradleBin;

    private static int compileMaxWaitTime;

    private static String binHome;

    @Value("${binHome}")
    public void setBinHome(String binHome) {
        CompileUtil.binHome = binHome;
    }

    @Value("${gradleBin}")
    public void setGradleBin(String gradleBin) {
        CompileUtil.gradleBin = gradleBin;
    }

    @Value("${compile.maxWaitTime}")
    public void setCompileMaxWaitTime(int compileMaxWaitTime) {
        CompileUtil.compileMaxWaitTime = compileMaxWaitTime;
    }

    public static boolean isCompilable(String repoPath, String repoUuid, String commit) {

        List<String> compilePathList = PomAnalysisUtil.getMainPom(getCompilePath(repoPath));
        if (compilePathList == null || compilePathList.isEmpty()) {
            if (findBuildGradle(repoPath)) {
                return gradleCompile(repoPath);
            } else {
                return true;
            }
        }

        for (String compilePath : compilePathList) {
            CompileToolEnum compileToolEnum = getCompileToolByPath(compilePath);
            log.info("Compile Path is {}", compilePath);
            log.info("Compile Tool is {}", compileToolEnum.name());
            // TODO 应该根据接口来动态的根据 compileTool 调用相应的实现方法
            if (compileToolEnum == CompileToolEnum.MAVEN) {
                if (!tryDifferentJdkCompile(compilePath, repoUuid, commit)) {
                    return false;
                }
            } else if (compileToolEnum == CompileToolEnum.GRADLE) {
                compilePath = compilePath.replace("build.gradle", "");
                if (!gradleCompile(compilePath)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean findBuildGradle(String repoPath) {
        File file = new File(repoPath + "/build.gradle");
        return file.exists();
    }

    public static boolean tryDifferentJdkCompile(String compilePath, String repoUuid, String commit) {
        String baseCommand = binHome + "mvn-compile2.sh " + compilePath.replace("/pom.xml", "") + " " + repoUuid + "_" + commit + " ";
        for (JdkVersionEnum jdkVersion : JdkVersionEnum.values()) {
            if (ShUtil.executeCommand(baseCommand + jdkVersion.getVersion(), compileMaxWaitTime)) {
                return true;
            }
        }
        return false;
    }

    private static boolean gradleCompile(String projectDirectory) {

        try {
            Runtime rt = Runtime.getRuntime();
            String command = gradleBin + " " + projectDirectory;
            log.info("command -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(compileMaxWaitTime, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("compile gradle timeout ! ({}s)", compileMaxWaitTime);
                return false;
            }
            log.info("exit value is {}", process.exitValue());
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("gradleCompile Exception!");
            return false;
        }
    }

    private static CompileToolEnum getCompileToolByPath(String compilePath) {
        for (CompileToolEnum compileToolEnum : CompileToolEnum.values()) {
            if (compilePath.endsWith(compileToolEnum.compileFile())) {
                return compileToolEnum;
            }
        }
        return CompileToolEnum.MAVEN;
    }

    private static List<String> getCompilePath(String repoPath) {
        File repoFile = new File(repoPath);
        // 查找可编译的文件
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> (file.isFile() && isContainsCompileFile(path)),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(repoFile);
        if (pathList.isEmpty()) {
            return new ArrayList<>();
        }
        return pathList;
    }

    private static boolean isContainsCompileFile(String path) {
        return path.endsWith(CompileToolEnum.MAVEN.compileFile());
    }

    public static boolean checkNeedCompile(String tool) {
        return true;
    }
}
