package com.example.sonarqubescan.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: keyon
 * @time: 2022/1/14 3:57 下午
 */
@Slf4j
public class PomAnalysisUtil {
    /**
     * 得到某个主要的pom文件  解决重复编译的问题
     *
     * @param pomPaths pom.xml 的绝对地址
     * @return 过滤后的pom文件
     */
    @SneakyThrows
    public static List<String> getMainPom(List<String> pomPaths) {
        if (pomPaths == null || pomPaths.size() < 2) {
            return pomPaths;
        }

        Set<String> finalPomPaths = new HashSet<>();
        for (String pomPath : pomPaths) {
            finalPomPaths.add(new File(pomPath).getCanonicalPath());
        }

        for (String pomPath : pomPaths) {
            if (!pomPath.endsWith("pom.xml")) {
                continue;
            }
            try (FileInputStream fis = new FileInputStream(pomPath)) {
                String sourcePath = pomPath.replace("pom.xml", "");
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(fis);

                for (String module : model.getModules()) {
                    String sonModulePath = sourcePath + module + "/pom.xml";
                    File sonPom = new File(sonModulePath);
                    if (sonPom.exists()) {
                        finalPomPaths.remove(sonPom.getCanonicalPath());
                    }
                }
            } catch (Exception e) {
                log.error("analyzed pom failed！");
                log.error(e.getMessage());
            }
        }

        return new ArrayList<>(finalPomPaths);
    }
}
