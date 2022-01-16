package com.example.sonarqubescan;

import com.example.sonarqubescan.jGitHelper.DiffFile;
import com.example.sonarqubescan.jGitHelper.JGitHelper;
import com.example.sonarqubescan.utils.StringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@MapperScan("com.example.sonarqubescan.mapper")
@SpringBootApplication
public class SonarQubeScanApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SonarQubeScanApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        JGitHelper jGitHelper = new JGitHelper("/Users/keyon/Documents/bigDataPlatform/sonarQubeScan");
//        DiffFile diffFile = jGitHelper.getAllDiffFilePair("9f0048d96e6de32af8ba984ca4594f26315b7d51");
//        List<String> filesToScan = new ArrayList<>();
//        filesToScan.addAll(diffFile.getAddFiles());
//        filesToScan.addAll(diffFile.getChangeFiles().values());
//        for(String file : filesToScan){
//            System.out.println(file);
//        }
       String ans = StringUtil.removePrefix("src/main/java/com/example/sonarqubescan/Mapper/LocationMapper.java","src/main/java/");



    }

}
