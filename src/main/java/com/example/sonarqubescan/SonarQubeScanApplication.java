package com.example.sonarqubescan;


import com.example.sonarqubescan.scan.Analyzer;
import com.example.sonarqubescan.utils.StringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;


@MapperScan("com.example.sonarqubescan.mapper")
@SpringBootApplication
public class SonarQubeScanApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SonarQubeScanApplication.class, args);
    }
    Analyzer analyzer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String repoPath = "/home/fdse/user/sxz/projects_meta/";
        List<List<String>> lists = StringUtil.readCsv("/home/fdse/user/sxz/datas.csv");
        for(List<String> list : lists){
            String projectName = list.get(0);
            projectName = projectName.replace('/','_');
            projectName = projectName.replace("\"", "");
            String commit = list.get(1);
            commit = commit.replace("\"", "");
            System.out.println(projectName);
            if(list.get(0) != null && list.get(0).length() != 0 && list.get(1) != null && list.get(0).length() != 1){
                analyzer.scan(repoPath + projectName + "/meta", projectName, commit);
            }

        }

//        JGitHelper jGitHelper = new JGitHelper("/Users/keyon/Documents/bigDataPlatform/issue/benchmarkTest/benchmarkissuetest");
//        jGitHelper.getAllDiffFilePair("9795a650775fdbe9a58c7f2af58a09dcb104ea65");
//        System.out.println(jGitHelper.getFileEdits().get("src/main/java/org/example/Match.java"));


    }
    @Autowired
    public void setAnalyzer(Analyzer analyzer){
        this.analyzer = analyzer;
    }


}
