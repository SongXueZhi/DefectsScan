package com.example.sonarqubescan;

import com.example.sonarqubescan.scan.Analyzer;
import com.example.sonarqubescan.utils.StringUtil;
import org.checkerframework.checker.units.qual.A;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@MapperScan("com.example.sonarqubescan.mapper")
@SpringBootApplication
public class SonarQubeScanApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SonarQubeScanApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Analyzer analyzer= new Analyzer();
        String repoPath = "/home/fdse/codeWisdom/repository/";
        List<List<String>> lists = StringUtil.readCsv("/Users/keyon/Downloads/datas.csv");
        for(List<String> list : lists){
            System.out.println(list);
            if(list.get(0) != null && list.get(0).length() != 0 && list.get(1) != null && list.get(0).length() != 1){
                analyzer.scan(repoPath + list.get(0), list.get(0), list.get(1));
            }

        }

        String ans = StringUtil.removePrefix("src/main/java/com/example/sonarqubescan/Mapper/LocationMapper.java","src/main/java/");




    }

}
